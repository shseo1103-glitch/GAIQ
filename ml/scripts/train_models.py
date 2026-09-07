#!/usr/bin/env python3
"""GAIQ ML 1단계 — 학습 스크립트.

ml/data/processed/<metric_code>.csv 를 읽어 타겟별로:
  - GaussianProcessRegressor(RBF/Matern 커널, StandardScaler 포함 Pipeline) 학습 (주모델)
  - 데이터 8건 이상이면 RandomForestRegressor 보조 학습
  - Leave-One-Out CV로 r2/mae/rmse 산출 (극소량 데이터이므로 train/test split 대신 LOO)
  - 학습된 모델을 ml/models/<metric_code>_<version>.pkl (joblib) 로 저장
  - 메타데이터를 ml/models/registry.json 에 기록 (ml_model_version 테이블과 1:1 대응 스키마)

metric_code 당 학습 데이터가 MIN_TRAINABLE(5) 미만이면 모델을 만들지 않고
RULE_BASED_FALLBACK 엔트리만 registry에 남긴다 (문서 권장안: "경험적 lookup + 불확실성 정량화").
"""
import json
import math
from datetime import datetime, timezone
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.gaussian_process import GaussianProcessRegressor
from sklearn.gaussian_process.kernels import RBF, Matern, WhiteKernel, ConstantKernel as C
from sklearn.model_selection import LeaveOneOut
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score

BASE_DIR = Path(__file__).resolve().parent.parent
PROCESSED_DIR = BASE_DIR / "data" / "processed"
MODELS_DIR = BASE_DIR / "models"
MODELS_DIR.mkdir(parents=True, exist_ok=True)

MIN_TRAINABLE = 5
MIN_FOR_RF = 8
VERSION = "v1"


def make_gpr_pipeline(n_features, kernel_name="matern"):
    if kernel_name == "matern":
        kernel = C(1.0, (1e-3, 1e3)) * Matern(length_scale=np.ones(n_features), nu=1.5) + WhiteKernel(1e-3, (1e-6, 1e1))
    else:
        kernel = C(1.0, (1e-3, 1e3)) * RBF(length_scale=np.ones(n_features)) + WhiteKernel(1e-3, (1e-6, 1e1))
    gpr = GaussianProcessRegressor(kernel=kernel, normalize_y=True, n_restarts_optimizer=8, random_state=42)
    return Pipeline([("scaler", StandardScaler()), ("gpr", gpr)]), kernel


def loo_cv(build_fn, X, y):
    """build_fn() -> unfitted estimator. LOO CV로 예측 배열 반환."""
    loo = LeaveOneOut()
    preds = np.zeros(len(y))
    for train_idx, test_idx in loo.split(X):
        est = build_fn()
        est.fit(X[train_idx], y[train_idx])
        preds[test_idx] = est.predict(X[test_idx])
    return preds


def metrics(y_true, y_pred):
    r2 = r2_score(y_true, y_pred) if len(y_true) > 1 else float("nan")
    mae = mean_absolute_error(y_true, y_pred)
    rmse = math.sqrt(mean_squared_error(y_true, y_pred))
    return r2, mae, rmse


def train_metric(metric_code, csv_path, registry):
    df = pd.read_csv(csv_path)
    n = len(df)
    feature_cols = [c for c in df.columns if c not in ("row_id", "y")]

    if n < MIN_TRAINABLE:
        registry[metric_code] = [{
            "target_metric_code": metric_code,
            "algorithm": "RULE_BASED_FALLBACK",
            "version_no": VERSION,
            "trained_at": datetime.now(timezone.utc).isoformat(),
            "training_data_count": n,
            "r2_score": None,
            "mae": None,
            "rmse": None,
            "kernel_type": None,
            "model_file_path": "N/A_RULE_BASED_FALLBACK",
            "is_active": True,
            "hyperparameters_json": {"reason": f"training_data_count({n}) < MIN_TRAINABLE({MIN_TRAINABLE})"},
            "features": feature_cols,
        }]
        print(f"[{metric_code}] n={n} < {MIN_TRAINABLE} -> RULE_BASED_FALLBACK (모델 생성 안 함)")
        return

    X = df[feature_cols].values.astype(float)
    y = df["y"].values.astype(float)

    entries = []

    # --- GPR (주모델) ---
    kernel_name = "matern"
    gpr_preds = loo_cv(lambda: make_gpr_pipeline(X.shape[1], kernel_name)[0], X, y)
    gpr_r2, gpr_mae, gpr_rmse = metrics(y, gpr_preds)

    final_gpr_pipeline, kernel_obj = make_gpr_pipeline(X.shape[1], kernel_name)
    final_gpr_pipeline.fit(X, y)
    gpr_model_path = MODELS_DIR / f"{metric_code}_GPR_{VERSION}.pkl"
    joblib.dump({
        "pipeline": final_gpr_pipeline,
        "feature_cols": feature_cols,
        "algorithm": "GPR",
        "metric_code": metric_code,
    }, gpr_model_path)

    entries.append({
        "target_metric_code": metric_code,
        "algorithm": "GPR",
        "version_no": VERSION,
        "trained_at": datetime.now(timezone.utc).isoformat(),
        "training_data_count": n,
        "r2_score": None if math.isnan(gpr_r2) else round(float(gpr_r2), 4),
        "mae": round(float(gpr_mae), 4),
        "rmse": round(float(gpr_rmse), 4),
        "kernel_type": f"Matern(nu=1.5)+WhiteKernel, fitted_kernel={final_gpr_pipeline.named_steps['gpr'].kernel_}",
        "model_file_path": str(gpr_model_path),
        "is_active": True,
        "hyperparameters_json": {
            "n_restarts_optimizer": 8,
            "normalize_y": True,
            "fitted_kernel_params": str(final_gpr_pipeline.named_steps["gpr"].kernel_.get_params()),
        },
        "features": feature_cols,
    })
    print(f"[{metric_code}] GPR: n={n} r2={gpr_r2:.4f} mae={gpr_mae:.4f} rmse={gpr_rmse:.4f} -> {gpr_model_path.name}")

    # --- RandomForest (보조 검증, n>=8일 때만) ---
    if n >= MIN_FOR_RF:
        def build_rf():
            return RandomForestRegressor(n_estimators=200, random_state=42, max_depth=4, min_samples_leaf=1)

        rf_preds = loo_cv(build_rf, X, y)
        rf_r2, rf_mae, rf_rmse = metrics(y, rf_preds)

        final_rf = build_rf()
        final_rf.fit(X, y)
        rf_model_path = MODELS_DIR / f"{metric_code}_RANDOM_FOREST_{VERSION}.pkl"
        joblib.dump({
            "model": final_rf,
            "feature_cols": feature_cols,
            "algorithm": "RANDOM_FOREST",
            "metric_code": metric_code,
        }, rf_model_path)

        entries.append({
            "target_metric_code": metric_code,
            "algorithm": "RANDOM_FOREST",
            "version_no": VERSION,
            "trained_at": datetime.now(timezone.utc).isoformat(),
            "training_data_count": n,
            "r2_score": None if math.isnan(rf_r2) else round(float(rf_r2), 4),
            "mae": round(float(rf_mae), 4),
            "rmse": round(float(rf_rmse), 4),
            "kernel_type": None,
            "model_file_path": str(rf_model_path),
            "is_active": False,  # 보조검증 모델, 주 서빙은 GPR
            "hyperparameters_json": {"n_estimators": 200, "max_depth": 4, "min_samples_leaf": 1},
            "features": feature_cols,
        })
        print(f"[{metric_code}] RandomForest(보조검증): n={n} r2={rf_r2:.4f} mae={rf_mae:.4f} rmse={rf_rmse:.4f} -> {rf_model_path.name}")
    else:
        print(f"[{metric_code}] n={n} < {MIN_FOR_RF} -> RandomForest 보조모델 생략 (GPR 단독)")

    registry[metric_code] = entries


def main():
    with open(PROCESSED_DIR / "manifest.json") as f:
        manifest = json.load(f)

    registry = {}
    for metric_code, info in manifest.items():
        train_metric(metric_code, info["csv_path"], registry)

    registry_path = MODELS_DIR / "registry.json"
    with open(registry_path, "w") as f:
        json.dump(registry, f, ensure_ascii=False, indent=2)

    print(f"\n=== registry.json 기록 완료: {registry_path} ===")
    for mc, entries in registry.items():
        for e in entries:
            print(f"  {mc} / {e['algorithm']} / n={e['training_data_count']} / r2={e['r2_score']} / active={e['is_active']}")


if __name__ == "__main__":
    main()
