from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import base64
import binascii
import io
import logging
import os
import re

logging.basicConfig(level=os.getenv("LOG_LEVEL", "INFO"))
logger = logging.getLogger("swasthyasetu-ai")
DEMO_MODE = os.getenv("AI_DEMO_MODE", "false").lower() == "true"

try:
    from PIL import Image, ImageFilter, ImageEnhance
    import pytesseract
    OCR_AVAILABLE = True
    logger.info("OCR engine available")
except ImportError:
    OCR_AVAILABLE = False
    logger.warning("OCR dependencies are unavailable")

app = FastAPI(title="SwasthyaSetu AI Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "ai-service",
        "ocrAvailable": OCR_AVAILABLE,
        "demoMode": DEMO_MODE,
    }


class Request(BaseModel):
    image: str


MEDICINES = {
    "paracetamol": "Paracetamol",
    "acetaminophen": "Paracetamol",
    "ibuprofen": "Ibuprofen",
    "aspirin": "Aspirin",
    "diclofenac": "Diclofenac",
    "naproxen": "Naproxen",
    "tramadol": "Tramadol",
    "codeine": "Codeine",
    "amoxicillin": "Amoxicillin",
    "amoxycillin": "Amoxicillin",
    "azithromycin": "Azithromycin",
    "ciprofloxacin": "Ciprofloxacin",
    "doxycycline": "Doxycycline",
    "metronidazole": "Metronidazole",
    "clarithromycin": "Clarithromycin",
    "levofloxacin": "Levofloxacin",
    "ceftriaxone": "Ceftriaxone",
    "augmentin": "Amoxicillin/Clavulanate",
    "metformin": "Metformin",
    "glibenclamide": "Glibenclamide",
    "glipizide": "Glipizide",
    "insulin": "Insulin",
    "sitagliptin": "Sitagliptin",
    "empagliflozin": "Empagliflozin",
    "amlodipine": "Amlodipine",
    "lisinopril": "Lisinopril",
    "losartan": "Losartan",
    "metoprolol": "Metoprolol",
    "betaloc": "Metoprolol (Betaloc)",
    "atenolol": "Atenolol",
    "enalapril": "Enalapril",
    "ramipril": "Ramipril",
    "oxprolol": "Oxprenolol",
    "oxprenolol": "Oxprenolol",
    "bisoprolol": "Bisoprolol",
    "carvedilol": "Carvedilol",
    "atorvastatin": "Atorvastatin",
    "rosuvastatin": "Rosuvastatin",
    "simvastatin": "Simvastatin",
    "clopidogrel": "Clopidogrel",
    "omeprazole": "Omeprazole",
    "pantoprazole": "Pantoprazole",
    "ranitidine": "Ranitidine",
    "cimetidine": "Cimetidine",
    "domperidone": "Domperidone",
    "ondansetron": "Ondansetron",
    "metoclopramide": "Metoclopramide",
    "esomeprazole": "Esomeprazole",
    "cetirizine": "Cetirizine",
    "loratadine": "Loratadine",
    "fexofenadine": "Fexofenadine",
    "montelukast": "Montelukast",
    "salbutamol": "Salbutamol",
    "prednisolone": "Prednisolone",
    "dexamethasone": "Dexamethasone",
    "hydroxychloroquine": "Hydroxychloroquine",
    "dorzolamide": "Dorzolamide",
    "timolol": "Timolol",
    "latanoprost": "Latanoprost",
    "vitamin": "Vitamin",
    "calcium": "Calcium",
    "iron": "Iron",
    "folic": "Folic Acid",
    "zinc": "Zinc",
    "vitamin d": "Vitamin D",
    "vitamin b": "Vitamin B Complex",
    "vitamin c": "Vitamin C",
    "levothyroxine": "Levothyroxine",
    "thyroxine": "Levothyroxine",
    "albendazole": "Albendazole",
    "mebendazole": "Mebendazole",
    "fluconazole": "Fluconazole",
    "acyclovir": "Acyclovir",
    "oseltamivir": "Oseltamivir",
    "diazepam": "Diazepam",
    "alprazolam": "Alprazolam",
    "sertraline": "Sertraline",
    "escitalopram": "Escitalopram",
    "amitriptyline": "Amitriptyline",
}

DOSAGE_PATTERN = re.compile(
    r"\b(\d+(?:\.\d+)?\s*(?:mg|mcg|g|ml|iu|units?|%|drops?))\b",
    re.IGNORECASE,
)

FREQUENCY_PATTERNS = [
    (r"\bTDS\b", "TDS — Three times daily"),
    (r"\bBID\b|\bBD\b", "BD — Twice daily"),
    (r"\bQID\b", "QID — Four times daily"),
    (r"\bOD\b|\bonce\s+daily\b", "OD — Once daily"),
    (r"\bSOS\b|\bPRN\b", "SOS — As needed"),
    (r"\bHS\b|\bat\s+bedtime\b", "HS — At bedtime"),
    (r"\btwice\s+daily\b|\btwice\s+a\s+day\b", "BD — Twice daily"),
    (r"\bthree\s+times\s+daily\b", "TDS — Three times daily"),
    (r"\bonce\s+a\s+day\b", "OD — Once daily"),
    (r"\bevery\s+(\d+)\s+hours?\b", None),
    (r"\bmorning\s+and\s+(night|evening)\b", "BD — Twice daily"),
]

DISEASE_MAP = {
    "Fever": ["paracetamol", "ibuprofen", "aspirin", "acetaminophen"],
    "Respiratory Infection": ["amoxicillin", "azithromycin", "doxycycline", "clarithromycin", "levofloxacin", "ceftriaxone", "amoxycillin", "augmentin"],
    "Diabetes": ["metformin", "glibenclamide", "insulin", "sitagliptin"],
    "Hypertension": ["amlodipine", "lisinopril", "losartan", "atenolol", "metoprolol", "betaloc", "ramipril", "bisoprolol"],
    "Acidity / GERD": ["omeprazole", "pantoprazole", "ranitidine", "cimetidine", "esomeprazole"],
    "Allergy": ["cetirizine", "loratadine", "fexofenadine", "montelukast"],
    "Glaucoma": ["dorzolamide", "timolol", "latanoprost"],
    "High Cholesterol": ["atorvastatin", "rosuvastatin", "simvastatin"],
    "Infection / Bacterial": ["ciprofloxacin", "metronidazole", "amoxicillin"],
    "Pain": ["diclofenac", "naproxen", "ibuprofen", "tramadol"],
}


def preprocess_image(image: Image.Image) -> Image.Image:
    img = image.convert("L")
    img = ImageEnhance.Contrast(img).enhance(2.5)
    img = img.filter(ImageFilter.SHARPEN)
    w, h = img.size
    return img.resize((w * 2, h * 2), Image.LANCZOS)


def extract_frequency(text: str) -> str:
    for pattern, label in FREQUENCY_PATTERNS:
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            if label is None:
                return f"Every {match.group(1)} hours"
            return label
    return "As directed"


def extract_dosage(line: str) -> str:
    match = DOSAGE_PATTERN.search(line)
    return match.group(0).strip() if match else "As prescribed"


def infer_diseases(found_keys: list) -> str:
    scores = {}
    for disease, keys in DISEASE_MAP.items():
        overlap = sum(1 for key in keys if key in found_keys)
        if overlap:
            scores[disease] = overlap
    if not scores:
        return "General"
    sorted_diseases = sorted(scores, key=scores.get, reverse=True)
    return ", ".join(sorted_diseases[:2])


def parse_prescription(text: str) -> dict:
    lines = [line.strip() for line in text.splitlines() if line.strip()]
    found_meds = []
    found_keys = []

    for index, line in enumerate(lines):
        line_lower = line.lower()
        for key, display in MEDICINES.items():
            if key in line_lower and key not in found_keys:
                dosage = extract_dosage(line)
                frequency = extract_frequency(line)
                if frequency == "As directed":
                    context = " ".join(lines[max(0, index - 1):min(len(lines), index + 2)])
                    frequency = extract_frequency(context)
                found_meds.append({
                    "name": display,
                    "dosage": dosage,
                    "frequency": frequency,
                })
                found_keys.append(key)
                break

    explicit_diagnosis = None
    for line in lines:
        match = re.search(r"diagnosis\s*[:\-]?\s*(.+)", line, re.IGNORECASE)
        if match:
            explicit_diagnosis = match.group(1).strip().rstrip(".")
            break

    disease = explicit_diagnosis or infer_diseases(found_keys)

    if not found_meds:
        found_meds = [{"name": "Unknown", "dosage": "As prescribed", "frequency": "As directed"}]

    logger.info(
        "Prescription parsed successfully: characters=%s lines=%s medicines=%s explicitDiagnosis=%s",
        len(text),
        len(lines),
        len(found_meds),
        explicit_diagnosis is not None,
    )

    return {
        "disease": disease,
        "medicines": found_meds,
        "rawText": text,
    }


@app.post("/process")
def process(req: Request):
    try:
        image_bytes = base64.b64decode(req.image, validate=True)
    except (binascii.Error, ValueError):
        raise HTTPException(status_code=400, detail="Invalid base64 image")

    if not OCR_AVAILABLE:
        if DEMO_MODE:
            logger.warning("OCR unavailable; explicit demo mode fallback used")
            return parse_prescription(_mock_text())
        raise HTTPException(status_code=503, detail="OCR service is unavailable")

    try:
        image = Image.open(io.BytesIO(image_bytes))
        image.verify()
        image = Image.open(io.BytesIO(image_bytes))
        processed = preprocess_image(image)

        config = "--oem 3 --psm 6"
        text_raw = pytesseract.image_to_string(image, config=config)
        text_processed = pytesseract.image_to_string(processed, config=config)
        raw_text = text_raw if len(text_raw.strip()) >= len(text_processed.strip()) else text_processed

        logger.info(
            "OCR completed: originalChars=%s processedChars=%s selectedChars=%s",
            len(text_raw),
            len(text_processed),
            len(raw_text),
        )
    except Exception:
        logger.exception("OCR processing failed without logging prescription contents")
        if DEMO_MODE:
            logger.warning("Explicit demo mode fallback used after OCR failure")
            return parse_prescription(_mock_text())
        raise HTTPException(status_code=422, detail="Prescription image could not be processed")

    if not raw_text.strip():
        if DEMO_MODE:
            logger.warning("Explicit demo mode fallback used after empty OCR result")
            return parse_prescription(_mock_text())
        raise HTTPException(status_code=422, detail="No readable prescription text was detected")

    return parse_prescription(raw_text)


def _mock_text() -> str:
    return (
        "SWASTHYASETU MEDICAL CENTRE\n"
        "PATIENT: Demo Patient  AGE: 34  DATE: 12-05-2026\n"
        "Rx\n"
        "1. Tab. Paracetamol 500mg - 1 tab BD (Twice daily)\n"
        "2. Tab. Amoxicillin 250mg - 1 tab TDS (Three times daily)\n"
        "Diagnosis: Fever, Upper Respiratory Infection\n"
        "Dr. Demo Doctor MD\n"
    )
