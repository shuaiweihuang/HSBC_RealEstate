# hpml_train.py
"""
House Price ML – Train a clean, production-ready regression model
Only uses meaningful business features (id columns are automatically dropped)

Allowed features:
    square_footage, bedrooms, bathrooms, year_built,
    lot_size, distance_to_city_center, school_rating
"""

import argparse
import json
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.linear_model import Ridge
from sklearn.metrics import mean_absolute_error, r2_score
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler


ALLOWED_FEATURES = {
    "square_footage",
    "bedrooms",
    "bathrooms",
    "year_built",
    "lot_size",
    "distance_to_city_center",
    "school_rating",
}


def hpml_train(
    data_path: Path,
    model_path: Path,
    meta_path: Path,
    target_name: str | None = None,
) -> None:
    # 1. Load data
    df = pd.read_csv(data_path)

    if df.shape[0] < 10:
        raise ValueError("Dataset too small – need at least 10 rows.")

    # 2. Auto-detect target column
    if target_name and target_name in df.columns:
        target = target_name
    elif "price" in df.columns:
        target = "price"
    elif "sale_price" in df.columns:
        target = "sale_price"
    else:
        target = df.columns[-1]  # last resort

    y = df[target].astype(float)

    # 3. 嚴格篩選特徵：只保留 ALLOWED_FEATURES 中實際存在的欄位
    available_features = [col for col in ALLOWED_FEATURES if col in df.columns]
    missing_features = ALLOWED_FEATURES - set(available_features)

    if missing_features:
        print(f"[Warning] These requested features are missing in data: {missing_features}")

    if not available_features:
        raise ValueError("No allowed features found in dataset!")

    X_raw = df[available_features].copy()

    # 4. 再次確認沒有偷偷把 id 之類的欄位混進來（雙重保險）
    forbidden_patterns = {"id", "index", "row", "listing"}
    leaked = [c for c in X_raw.columns if any(p in c.lower() for p in forbidden_patterns)]
    if leaked:
        print(f"[Warning] Detected and auto-dropped forbidden columns: {leaked}")
        X_raw = X_raw.drop(columns=leaked)

    # 5. 分離數值與類別欄位（目前這 7 個全是 numeric，但保留彈性）
    numeric_cols = X_raw.select_dtypes(include=[np.number]).columns.tolist()
    categorical_cols = X_raw.select_dtypes(exclude=[np.number]).columns.tolist()

    # 6. Preprocessing
    numeric_transformer = Pipeline(steps=[("scaler", StandardScaler())])

    categorical_transformer = Pipeline(steps=[
        ("onehot", OneHotEncoder(handle_unknown="ignore", sparse_output=False))
    ])

    preprocessor = ColumnTransformer(
        transformers=[
            ("num", numeric_transformer, numeric_cols),
            ("cat", categorical_transformer, categorical_cols),
        ],
        sparse_threshold=0,
    )

    # 7. Final pipeline
    pipeline = Pipeline(steps=[
        ("preprocessor", preprocessor),
        ("model", Ridge(alpha=1.0)),
    ])

    # 8. Train & evaluate on training set (for reporting)
    pipeline.fit(X_raw, y)
    preds = pipeline.predict(X_raw)

    mae = float(mean_absolute_error(y, preds))
    r2 = float(r2_score(y, preds))
    train_mean_price = float(y.mean())
    naive_mae = float(mean_absolute_error(y, np.full_like(y, train_mean_price)))

    # 9. Extract coefficients (very useful for business explanation)
    coef_map = {}
    intercept = None
    try:
        feature_names = pipeline.named_steps["preprocessor"].get_feature_names_out()
        coefs = pipeline.named_steps["model"].coef_
        intercept = float(pipeline.named_steps["model"].intercept_)

        for name, coef in zip(feature_names, coefs):
            coef_map[name] = float(coef)

        # Print top 10 most important features
        top10 = sorted(coef_map.items(), key=lambda x: abs(x[1]), reverse=True)[:10]
        print("\nTop 10 feature coefficients:")
        for name, coef in top10:
            print(f"   {name}: {coef:+.2f}")
    except Exception as e:
        print(f"[Warning] Could not extract coefficients: {e}")

    # 10. Save model + metadata
    joblib.dump({
        "pipeline": pipeline,
        "features_used": X_raw.columns.tolist(),   # 明確記錄實際用了哪些欄位
        "target": target,
    }, model_path)

    meta = {
        "target": target,
        "n_samples": int(df.shape[0]),
        "features_used": X_raw.columns.tolist(),
        "train_mean_price": train_mean_price,
        "baseline_naive_mae": naive_mae,
        "metrics_on_training_set": {
            "mae": mae,
            "r2": r2,
            "rmse": float(np.sqrt(((y - preds) ** 2).mean())),
        },
        "model": "Ridge(alpha=1.0)",
        "coefficients": coef_map,
        "intercept": intercept,
    }

    with open(meta_path, "w", encoding="utf-8") as f:
        json.dump(meta, f, indent=2)

    # 11. Summary output
    print(f"\nModel saved -> {model_path}")
    print(f"Meta   saved -> {meta_path}")
    print(f"Used features ({len(X_raw.columns)}): {list(X_raw.columns)}")
    print(f"Training MAE : {mae:,.2f}   |   R²: {r2:.4f}")
    print(f"Naive MAE    : {naive_mae:,.2f}   (model improvement: {naive_mae - mae:,.2f})")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Train clean house price model (id-safe)")
    parser.add_argument("--data", type=Path, default="../data/HousePriceDataset.csv")
    parser.add_argument("--model", type=Path, default="app/model.joblib")
    parser.add_argument("--meta", type=Path, default="app/model_meta.json")
    parser.add_argument("--target", type=str, default=None)
    args = parser.parse_args()

    hpml_train(args.data, args.model, args.meta, args.target)
