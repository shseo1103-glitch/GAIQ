"""GAIQ ML 1단계 — 추론 서비스 로직.

registry.json 에서 active(is_active=True) GPR 모델을 로드하여 예측값+표준편차를 산출.
RandomForest는 트리간 분산으로 보조 불확실성을 제공(가능한 경우 함께 반환).

confidence_level 규칙 (predicted_std_dev / training_data_count 기준):
  - 모델 없음 또는 학습건수 < 5           -> DATA_INSUFFICIENT
  - std_dev/training_data_count 낮음(<0.05)  -> HIGH
  - 중간(<0.2)                              -> MEDIUM
  - 그 외                                    -> LOW
"""
import json
from pathlib import Path

import joblib
import numpy as np

BASE_DIR = Path(__file__).resolve().parent.parent
REGISTRY_PATH = BASE_DIR / "models" / "registry.json"

MIN_TRAINABLE = 5

_model_cache = {}


def _load_registry():
    if not REGISTRY_PATH.exists():
        return {}
    with open(REGISTRY_PATH) as f:
        return json.load(f)


def _load_model_file(path_str):
    if path_str in _model_cache:
        return _model_cache[path_str]
    obj = joblib.load(path_str)
    _model_cache[path_str] = obj
    return obj


def _confidence_level(std_dev, training_data_count):
    if training_data_count is None or training_data_count < MIN_TRAINABLE:
        return "DATA_INSUFFICIENT"
    if std_dev is None:
        return "MEDIUM"
    ratio = abs(std_dev) / max(training_data_count, 1)
    if ratio < 0.05:
        return "HIGH"
    if ratio < 0.2:
        return "MEDIUM"
    return "LOW"


def get_registry_entry(target_metric_code):
    registry = _load_registry()
    entries = registry.get(target_metric_code)
    if not entries:
        return None, None
    gpr_entry = next((e for e in entries if e["algorithm"] == "GPR" and e.get("is_active")), None)
    rf_entry = next((e for e in entries if e["algorithm"] == "RANDOM_FOREST"), None)
    if gpr_entry is None:
        # GPR 없으면(RULE_BASED_FALLBACK만 있는 경우) fallback entry 자체를 반환
        fallback_entry = next((e for e in entries if e["algorithm"] == "RULE_BASED_FALLBACK"), None)
        return fallback_entry, rf_entry
    return gpr_entry, rf_entry


def predict(target_metric_code: str, input_params: dict):
    """
    Returns dict:
      {
        "targetMetricCode": ...,
        "algorithm": "GPR" | "RULE_BASED_FALLBACK",
        "predictedValue": float | None,
        "predictedStdDev": float | None,
        "confidenceLevel": "HIGH"|"MEDIUM"|"LOW"|"DATA_INSUFFICIENT",
        "trainingDataCount": int,
        "modelVersion": str | None,
        "auxRandomForest": {"predictedValue":..., "predictedStdDev":...} | None,
        "missingFeatures": [str] (입력에 없는데 모델이 필요로 하는 피처),
      }
    """
    gpr_entry, rf_entry = get_registry_entry(target_metric_code)

    if gpr_entry is None:
        return {
            "targetMetricCode": target_metric_code,
            "algorithm": "NONE",
            "predictedValue": None,
            "predictedStdDev": None,
            "confidenceLevel": "DATA_INSUFFICIENT",
            "trainingDataCount": 0,
            "modelVersion": None,
            "auxRandomForest": None,
            "missingFeatures": [],
            "message": f"No model or fallback entry registered for metric_code={target_metric_code}",
        }

    if gpr_entry["algorithm"] == "RULE_BASED_FALLBACK":
        return {
            "targetMetricCode": target_metric_code,
            "algorithm": "RULE_BASED_FALLBACK",
            "predictedValue": None,
            "predictedStdDev": None,
            "confidenceLevel": "DATA_INSUFFICIENT",
            "trainingDataCount": gpr_entry.get("training_data_count", 0),
            "modelVersion": gpr_entry.get("version_no"),
            "auxRandomForest": None,
            "missingFeatures": [],
            "message": "학습 데이터 부족(RULE_BASED_FALLBACK). 백엔드에서 threshold/최근실측 폴백을 사용해야 함.",
        }

    feature_cols = gpr_entry["features"]
    missing = [f for f in feature_cols if f not in input_params]

    result = {
        "targetMetricCode": target_metric_code,
        "algorithm": "GPR",
        "predictedValue": None,
        "predictedStdDev": None,
        "confidenceLevel": "DATA_INSUFFICIENT",
        "trainingDataCount": gpr_entry.get("training_data_count", 0),
        "modelVersion": gpr_entry.get("version_no"),
        "auxRandomForest": None,
        "missingFeatures": missing,
    }

    if missing:
        result["message"] = f"Missing required input features: {missing}"
        return result

    x_row = np.array([[float(input_params[f]) for f in feature_cols]])

    gpr_obj = _load_model_file(gpr_entry["model_file_path"])
    pipeline = gpr_obj["pipeline"]
    scaler = pipeline.named_steps["scaler"]
    gpr = pipeline.named_steps["gpr"]
    x_scaled = scaler.transform(x_row)
    mean, std = gpr.predict(x_scaled, return_std=True)

    predicted_value = float(mean[0])
    predicted_std_dev = float(std[0])
    training_data_count = gpr_entry.get("training_data_count", 0)

    result["predictedValue"] = round(predicted_value, 4)
    result["predictedStdDev"] = round(predicted_std_dev, 4)
    result["confidenceLevel"] = _confidence_level(predicted_std_dev, training_data_count)

    if rf_entry is not None:
        rf_obj = _load_model_file(rf_entry["model_file_path"])
        rf_model = rf_obj["model"]
        rf_feature_cols = rf_obj["feature_cols"]
        x_row_rf = np.array([[float(input_params[f]) for f in rf_feature_cols]])
        tree_preds = np.array([est.predict(x_row_rf)[0] for est in rf_model.estimators_])
        rf_mean = float(tree_preds.mean())
        rf_std = float(tree_preds.std())
        result["auxRandomForest"] = {
            "predictedValue": round(rf_mean, 4),
            "predictedStdDev": round(rf_std, 4),
            "trainingDataCount": rf_entry.get("training_data_count", 0),
        }

    return result
