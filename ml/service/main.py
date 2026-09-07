"""GAIQ ML 1단계 — FastAPI 추론 서비스.

실행:
  .venv/bin/uvicorn service.main:app --host 0.0.0.0 --port 8115

엔드포인트:
  GET  /health                -> 헬스체크
  POST /predict                -> {targetMetricCode, inputParams} -> 예측값+표준편차+신뢰도
"""
from typing import Any, Dict, Optional

from fastapi import FastAPI
from pydantic import BaseModel

from service.predict_service import predict as predict_impl

app = FastAPI(title="GAIQ ML Prediction Service", version="1.0.0")


class PredictRequest(BaseModel):
    targetMetricCode: str
    inputParams: Dict[str, Any]


class AuxRandomForest(BaseModel):
    predictedValue: Optional[float] = None
    predictedStdDev: Optional[float] = None
    trainingDataCount: Optional[int] = None


class PredictResponse(BaseModel):
    targetMetricCode: str
    algorithm: str
    predictedValue: Optional[float] = None
    predictedStdDev: Optional[float] = None
    confidenceLevel: str
    trainingDataCount: int
    modelVersion: Optional[str] = None
    auxRandomForest: Optional[AuxRandomForest] = None
    missingFeatures: Optional[list] = None
    message: Optional[str] = None


@app.get("/health")
def health():
    return {"status": "UP", "service": "gaiq-ml-predict"}


@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest):
    result = predict_impl(req.targetMetricCode, req.inputParams)
    return result
