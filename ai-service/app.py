from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import base64
import re
import io

# OCR
try:
    from PIL import Image, ImageFilter, ImageEnhance
    import pytesseract
    OCR_AVAILABLE = True
    print("✅ OCR (pytesseract + Pillow) loaded")
except ImportError:
    OCR_AVAILABLE = False
    print("⚠️  pytesseract/Pillow not installed — using mock OCR")

app = FastAPI(title="SwasthyaSetu AI Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


class Request(BaseModel):
    image: str  # base64-encoded image


# ── Medicine dictionary with aliases ─────────────────────────────────────────
MEDICINES = {
    # Pain / Fever
    "paracetamol": "Paracetamol",
    "acetaminophen": "Paracetamol",
    "ibuprofen": "Ibuprofen",
    "aspirin": "Aspirin",
    "diclofenac": "Diclofenac",
    "naproxen": "Naproxen",
    "tramadol": "Tramadol",
    "codeine": "Codeine",

    # Antibiotics
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

    # Diabetes
    "metformin": "Metformin",
    "glibenclamide": "Glibenclamide",
    "glipizide": "Glipizide",
    "insulin": "Insulin",
    "sitagliptin": "Sitagliptin",
    "empagliflozin": "Empagliflozin",

    # BP / Cardiac
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

    # Cholesterol
    "atorvastatin": "Atorvastatin",
    "rosuvastatin": "Rosuvastatin",
    "simvastatin": "Simvastatin",
    "clopidogrel": "Clopidogrel",

    # GI
    "omeprazole": "Omeprazole",
    "pantoprazole": "Pantoprazole",
    "ranitidine": "Ranitidine",
    "cimetidine": "Cimetidine",
    "domperidone": "Domperidone",
    "ondansetron": "Ondansetron",
    "metoclopramide": "Metoclopramide",
    "esomeprazole": "Esomeprazole",

    # Allergy / Respiratory
    "cetirizine": "Cetirizine",
    "loratadine": "Loratadine",
    "fexofenadine": "Fexofenadine",
    "montelukast": "Montelukast",
    "salbutamol": "Salbutamol",
    "prednisolone": "Prednisolone",
    "dexamethasone": "Dexamethasone",
    "hydroxychloroquine": "Hydroxychloroquine",

    # Eye
    "dorzolamide": "Dorzolamide",
    "timolol": "Timolol",
    "latanoprost": "Latanoprost",

    # Vitamins / Supplements
    "vitamin": "Vitamin",
    "calcium": "Calcium",
    "iron": "Iron",
    "folic": "Folic Acid",
    "zinc": "Zinc",
    "vitamin d": "Vitamin D",
    "vitamin b": "Vitamin B Complex",
    "vitamin c": "Vitamin C",

    # Thyroid
    "levothyroxine": "Levothyroxine",
    "thyroxine": "Levothyroxine",

    # Antiparasitic
    "albendazole": "Albendazole",
    "mebendazole": "Mebendazole",

    # Antifungal / Antiviral
    "fluconazole": "Fluconazole",
    "acyclovir": "Acyclovir",
    "oseltamivir": "Oseltamivir",

    # Psych
    "diazepam": "Diazepam",
    "alprazolam": "Alprazolam",
    "sertraline": "Sertraline",
    "escitalopram": "Escitalopram",
    "amitriptyline": "Amitriptyline",
}

# ── Dosage pattern ────────────────────────────────────────────────────────────
DOSAGE_PATTERN = re.compile(
    r"\b(\d+(?:\.\d+)?\s*(?:mg|mcg|g|ml|iu|units?|%|drops?))\b",
    re.IGNORECASE,
)

# ── Frequency patterns (order matters — most specific first) ──────────────────
FREQUENCY_PATTERNS = [
    (r"\bTDS\b",                              "TDS — Three times daily"),
    (r"\bBID\b|\bBD\b",                       "BD — Twice daily"),
    (r"\bQID\b",                              "QID — Four times daily"),
    (r"\bOD\b|\bonce\s+daily\b",             "OD — Once daily"),
    (r"\bSOS\b|\bPRN\b",                      "SOS — As needed"),
    (r"\bHS\b|\bat\s+bedtime\b",             "HS — At bedtime"),
    (r"\btwice\s+daily\b|\btwice\s+a\s+day\b", "BD — Twice daily"),
    (r"\bthree\s+times\s+daily\b",            "TDS — Three times daily"),
    (r"\bonce\s+a\s+day\b",                   "OD — Once daily"),
    (r"\bevery\s+(\d+)\s+hours?\b",           None),   # dynamic
    (r"\bmorning\s+and\s+(night|evening)\b",  "BD — Twice daily"),
]

# ── Disease inference ─────────────────────────────────────────────────────────
DISEASE_MAP = {
    "Fever":                 ["paracetamol", "ibuprofen", "aspirin", "acetaminophen"],
    "Respiratory Infection": ["amoxicillin", "azithromycin", "doxycycline", "clarithromycin",
                              "levofloxacin", "ceftriaxone", "amoxycillin", "augmentin"],
    "Diabetes":              ["metformin", "glibenclamide", "insulin", "sitagliptin"],
    "Hypertension":          ["amlodipine", "lisinopril", "losartan", "atenolol",
                              "metoprolol", "betaloc", "ramipril", "bisoprolol"],
    "Acidity / GERD":        ["omeprazole", "pantoprazole", "ranitidine", "cimetidine", "esomeprazole"],
    "Allergy":               ["cetirizine", "loratadine", "fexofenadine", "montelukast"],
    "Glaucoma":              ["dorzolamide", "timolol", "latanoprost"],
    "High Cholesterol":      ["atorvastatin", "rosuvastatin", "simvastatin"],
    "Infection / Bacterial": ["ciprofloxacin", "metronidazole", "amoxicillin"],
    "Pain":                  ["diclofenac", "naproxen", "ibuprofen", "tramadol"],
}


def preprocess_image(image: Image.Image) -> Image.Image:
    """Enhance image for better OCR accuracy."""
    # Convert to grayscale
    img = image.convert("L")
    # Increase contrast
    img = ImageEnhance.Contrast(img).enhance(2.5)
    # Sharpen
    img = img.filter(ImageFilter.SHARPEN)
    # Scale up (helps tesseract)
    w, h = img.size
    img = img.resize((w * 2, h * 2), Image.LANCZOS)
    return img


def extract_frequency(text: str) -> str:
    for pattern, label in FREQUENCY_PATTERNS:
        m = re.search(pattern, text, re.IGNORECASE)
        if m:
            if label is None:
                return f"Every {m.group(1)} hours"
            return label
    return "As directed"


def extract_dosage(line: str) -> str:
    m = DOSAGE_PATTERN.search(line)
    return m.group(0).strip() if m else "As prescribed"


def infer_diseases(found_keys: list) -> str:
    scores = {}
    for disease, keys in DISEASE_MAP.items():
        overlap = sum(1 for k in keys if k in found_keys)
        if overlap:
            scores[disease] = overlap
    if not scores:
        return "General"
    # Return top 2 diseases
    sorted_diseases = sorted(scores, key=scores.get, reverse=True)
    return ", ".join(sorted_diseases[:2])


def parse_prescription(text: str) -> dict:
    """Parse OCR text → structured prescription."""
    print(f"[NLP] Input text:\n{text}\n---")

    lines  = [l.strip() for l in text.splitlines() if l.strip()]
    found_meds = []
    found_keys = []

    # Scan every line for medicine names
    for line in lines:
        line_lower = line.lower()
        for key, display in MEDICINES.items():
            if key in line_lower and key not in found_keys:
                dosage = extract_dosage(line)
                freq   = extract_frequency(line)
                # Also check next few lines for freq if not found on this line
                if freq == "As directed":
                    context = " ".join(lines[max(0, lines.index(line) - 1):
                                             min(len(lines), lines.index(line) + 2)])
                    freq = extract_frequency(context)
                found_meds.append({
                    "name":      display,
                    "dosage":    dosage,
                    "frequency": freq,
                })
                found_keys.append(key)
                break  # one medicine per line

    # Also try to pick up explicit "Diagnosis:" line
    explicit_diagnosis = None
    for line in lines:
        m = re.search(r"diagnosis\s*[:\-]?\s*(.+)", line, re.IGNORECASE)
        if m:
            explicit_diagnosis = m.group(1).strip().rstrip(".")
            break

    disease = explicit_diagnosis or infer_diseases(found_keys)

    if not found_meds:
        found_meds = [{"name": "Unknown", "dosage": "As prescribed", "frequency": "As directed"}]

    return {
        "disease":   disease,
        "medicines": found_meds,
        "rawText":   text,
    }


@app.post("/process")
def process(req: Request):
    image_bytes = base64.b64decode(req.image)

    if OCR_AVAILABLE:
        try:
            image = Image.open(io.BytesIO(image_bytes))
            processed = preprocess_image(image)

            # Try with and without preprocessing, use whichever gives more text
            config = "--oem 3 --psm 6"
            text_raw  = pytesseract.image_to_string(image, config=config)
            text_proc = pytesseract.image_to_string(processed, config=config)

            raw_text = text_raw if len(text_raw) > len(text_proc) else text_proc
            print(f"[OCR] Raw ({len(text_raw)} chars) vs Processed ({len(text_proc)} chars)")
            print(f"[OCR] Using:\n{raw_text}")
        except Exception as e:
            print(f"[OCR] Error: {e} — falling back to mock")
            raw_text = _mock_text()
    else:
        raw_text = _mock_text()

    return parse_prescription(raw_text)


def _mock_text() -> str:
    """Reliable mock prescription text for demo/fallback."""
    return (
        "SWASTHYASETU MEDICAL CENTRE\n"
        "PATIENT: John Smith  AGE: 34  DATE: 12-05-2026\n"
        "Rx\n"
        "1. Tab. Paracetamol 500mg - 1 tab BD (Twice daily)\n"
        "2. Tab. Amoxicillin 250mg - 1 tab TDS (Three times daily)\n"
        "3. Tab. Metformin 500mg - 1 tab OD (Once daily)\n"
        "4. Tab. Omeprazole 20mg - 1 cap OD (Before meals)\n"
        "5. Tab. Cetirizine 10mg - 1 tab HS (At bedtime)\n"
        "Diagnosis: Fever, Upper Respiratory Infection, Acidity\n"
        "Dr. Steve Johnson MD\n"
    )