#!/usr/bin/env python3
"""GAIQ ML 1단계 — 데이터 전처리 스크립트.

experiment_dataset_raw.json(구조화 버전)을 읽어, 타겟 물성치(metric_code)별로
학습 가능한 (공정파라미터 X, 측정값 y) 쌍을 추출하여
ml/data/processed/<metric_code>.csv 로 저장한다.

CSV보다 JSON이 각 실험의 process/measured_properties 구조를 그대로 보존하고 있어
정규식 추출 및 결측치 판별이 더 명확하므로 JSON을 1차 소스로 사용한다.

추출 대상 (docs/recon/01-data-source-analysis.md §5 근거):
  - SURFACE_TEMP_C: 부스바 전류인가 온도상승 시험 (row 20-28).
      전류(A)는 process.other_conditions 텍스트에서 정규식으로 추출
      (예: "100Adc(50% of 200A rated)" -> 100).
      부스바 규격(SQ)은 process.substrate 텍스트에서 추출 (예: "부스바 80SQ" -> 80).
      y는 해당 row에 존재하는 온도상승 측정치들(general/graphene x contact/IR,
      또는 single/multi x general/graphene)의 평균값으로 산출 —
      그래핀 처리 여부와 무관하게 "해당 전류에서의 대표 표면온도상승"을 타겟으로 함.
  - RAMAN_ID_IG: process.temperature_C / reaction_time_min -> measured.raman_ID_IG.
      부등호("<=0.1")로 표기된 값은 정확한 수치가 아니므로 학습쌍에서 제외.
  - RAMAN_I2D_IG: 위와 동일한 방식, measured.raman_I2D_IG 대상.

출력:
  - ml/data/processed/<metric_code>.csv (row_id, feature.., y)
  - ml/data/processed/manifest.json (metric_code별 요약: count, features, sufficient)
"""
import json
import re
import csv
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_JSON = BASE_DIR.parent / "data" / "experiment_dataset_raw.json"
OUT_DIR = BASE_DIR / "data" / "processed"
OUT_DIR.mkdir(parents=True, exist_ok=True)

MIN_TRAINABLE = 5  # 미달 시 RULE_BASED_FALLBACK 대상으로 남김 (모델 강제 생성 금지)


def _num(x):
    """문자열/리스트/숫자를 단일 float로 정규화. 실패 시 None."""
    if x is None:
        return None
    if isinstance(x, (int, float)):
        return float(x)
    if isinstance(x, list):
        vals = [_num(v) for v in x]
        vals = [v for v in vals if v is not None]
        return sum(vals) / len(vals) if vals else None
    if isinstance(x, str):
        s = x.strip()
        if "<=" in s or ">=" in s or "<" in s or ">" in s or "+-" in s or "±" in s:
            # 부등호/오차범위 표기 -> 정확한 수치 아님, 학습쌍에서 제외
            return None
        m = re.match(r"^-?\d+(\.\d+)?$", s)
        if m:
            return float(s)
        return None
    return None


def extract_current_A(text):
    if not text:
        return None
    m = re.search(r"(\d+)\s*A\s*dc", text, re.IGNORECASE)
    if m:
        return float(m.group(1))
    m = re.search(r"(\d+)\s*A(?!h)", text, re.IGNORECASE)
    return float(m.group(1)) if m else None


def extract_sq(text):
    if not text:
        return None
    m = re.search(r"(\d+)\s*SQ", text, re.IGNORECASE)
    return float(m.group(1)) if m else None


def extract_growth_time_min(reaction_time_min):
    """'Anneal 90 + Growth 60' 같은 텍스트에서 Growth 단계 시간(분)만 추출.
    이미 순수 숫자면 그대로 반환."""
    if reaction_time_min is None:
        return None
    if isinstance(reaction_time_min, (int, float)):
        return float(reaction_time_min)
    if isinstance(reaction_time_min, str):
        m = re.search(r"Growth\s*(\d+)", reaction_time_min, re.IGNORECASE)
        if m:
            return float(m.group(1))
        m = re.match(r"^\d+$", reaction_time_min.strip())
        if m:
            return float(reaction_time_min.strip())
    return None


def build_surface_temp_c(experiments):
    """부스바 전류인가 온도상승: X=[current_A, busbar_sq], y=avg(temp_rise 계열)."""
    rows = []
    temp_keys_group_a = [
        "temp_rise_C_general_contact", "temp_rise_C_general_IR",
        "temp_rise_C_graphene_contact", "temp_rise_C_graphene_IR",
    ]
    temp_keys_group_b = [
        "temp_rise_C_single_general", "temp_rise_C_single_graphene",
        "temp_rise_C_multi_general", "temp_rise_C_multi_graphene",
    ]
    for e in experiments:
        proc = e.get("process") or {}
        meas = e.get("measured_properties") or {}
        current_A = extract_current_A(proc.get("other_conditions"))
        busbar_sq = extract_sq(proc.get("substrate"))
        if current_A is None or busbar_sq is None:
            continue
        vals = []
        for k in temp_keys_group_a + temp_keys_group_b:
            v = _num(meas.get(k))
            if v is not None:
                vals.append(v)
        if not vals:
            continue
        y = sum(vals) / len(vals)
        rows.append({
            "row_id": e["row_id"],
            "current_A": current_A,
            "busbar_sq": busbar_sq,
            "y": round(y, 4),
        })
    return rows, ["current_A", "busbar_sq"]


def build_raman_metric(experiments, measured_key):
    """RAMAN_ID_IG / RAMAN_I2D_IG: X=[temperature_C, growth_time_min], y=측정치.
    부등호 표기 값(<=0.1 등)은 정확치가 아니므로 제외."""
    rows = []
    for e in experiments:
        proc = e.get("process") or {}
        meas = e.get("measured_properties") or {}
        temp_c = _num(proc.get("temperature_C"))
        growth_min = extract_growth_time_min(proc.get("reaction_time_min"))
        y = _num(meas.get(measured_key))
        if temp_c is None or growth_min is None or y is None:
            continue
        rows.append({
            "row_id": e["row_id"],
            "temperature_C": temp_c,
            "growth_time_min": growth_min,
            "y": y,
        })
    return rows, ["temperature_C", "growth_time_min"]


def write_csv(metric_code, rows, features):
    path = OUT_DIR / f"{metric_code}.csv"
    with open(path, "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=["row_id"] + features + ["y"])
        w.writeheader()
        for r in rows:
            w.writerow(r)
    return str(path)


def main():
    with open(DATA_JSON) as f:
        data = json.load(f)
    experiments = data["experiments"]

    manifest = {}

    surf_rows, surf_features = build_surface_temp_c(experiments)
    surf_path = write_csv("SURFACE_TEMP_C", surf_rows, surf_features)
    manifest["SURFACE_TEMP_C"] = {
        "features": surf_features,
        "count": len(surf_rows),
        "sufficient": len(surf_rows) >= MIN_TRAINABLE,
        "csv_path": surf_path,
        "row_ids": [r["row_id"] for r in surf_rows],
        "note": "부스바 80SQ(row20-25) + 20SQ(row26-28) 결합, current_A+busbar_sq 2피처",
    }

    for metric_code, key in [("RAMAN_ID_IG", "raman_ID_IG"), ("RAMAN_I2D_IG", "raman_I2D_IG")]:
        rows, features = build_raman_metric(experiments, key)
        path = write_csv(metric_code, rows, features)
        manifest[metric_code] = {
            "features": features,
            "count": len(rows),
            "sufficient": len(rows) >= MIN_TRAINABLE,
            "csv_path": path,
            "row_ids": [r["row_id"] for r in rows],
            "note": "온도/시간 매칭 row만 채택, 부등호(<=) 표기 값은 제외",
        }

    with open(OUT_DIR / "manifest.json", "w") as f:
        json.dump(manifest, f, ensure_ascii=False, indent=2)

    print("=== prepare_dataset.py 결과 ===")
    for mc, info in manifest.items():
        status = "TRAINABLE" if info["sufficient"] else f"DATA_INSUFFICIENT(<{MIN_TRAINABLE})"
        print(f"  {mc}: n={info['count']} features={info['features']} rows={info['row_ids']} -> {status}")


if __name__ == "__main__":
    main()
