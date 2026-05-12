from fastapi import FastAPI
from pydantic import BaseModel
import base64

app = FastAPI()

class Request(BaseModel):
    image: str

@app.post("/process")
def process(req: Request):

    # Step 1: Decode image (for future use)
    image_bytes = base64.b64decode(req.image)

    # Step 2: OCR (TEMP MOCK)
    text = "Paracetamol 500mg BD for fever"

    # Step 3: NLP (TEMP MOCK)
    return {
        "disease": "Fever",
        "medicines": [
            {
                "name": "Paracetamol",
                "dosage": "500mg",
                "frequency": "BD"
            }
        ],
        "rawText": text
    }