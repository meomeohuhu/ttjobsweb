from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification

# Load model once at startup to avoid reloading each request.
MODEL_DIR = "/app/models"
tokenizer = AutoTokenizer.from_pretrained(MODEL_DIR)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_DIR)
model.eval()

app = FastAPI()


class PredictRequest(BaseModel):
    cvText: str


@app.post("/ai/predict")
def predict(request: PredictRequest):
    if not request.cvText or not request.cvText.strip():
        raise HTTPException(status_code=400, detail="cvText is required")

    inputs = tokenizer(
        request.cvText,
        truncation=True,
        max_length=512,
        return_tensors="pt",
    )
    with torch.no_grad():
        outputs = model(**inputs)
        probs = torch.softmax(outputs.logits, dim=-1).squeeze(0)

    # Use model's label mapping when available.
    id2label = model.config.id2label or {}
    scores = probs.tolist()
    pairs = []
    for idx, score in enumerate(scores):
        label = id2label.get(idx, str(idx))
        pairs.append([label, float(score)])

    # Sort by score desc and return top 3.
    pairs.sort(key=lambda x: x[1], reverse=True)
    return pairs[:3]
