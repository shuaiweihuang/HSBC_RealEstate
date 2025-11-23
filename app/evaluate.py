# evaluate.py  （最終專業版）
import joblib
import json
from pathlib import Path
import pandas as pd
import numpy as np
from sklearn.metrics import mean_absolute_error, r2_score


def evaluate(data_path: Path):
    model_path = Path("app/model.joblib")
    meta_path = Path("app/model_meta.json")

    # 1. 載入模型
    bundle = joblib.load(model_path)
    pipeline = bundle["pipeline"]
    features_used = bundle.get("features_used") or bundle.get("features", [])
    target = bundle.get("target", "price")

    print(f"Loading model that uses {len(features_used)} features: {features_used}")

    # 2. 載入預測資料
    df = pd.read_csv(data_path)
    X = df[features_used]

    # 3. 直接預測
    preds = pipeline.predict(X)

    # 4. 讀取訓練時的基準表現
    with open(meta_path) as f:
        meta = json.load(f)
    train_mean_price = meta["train_mean_price"]

    # 5. 輸出報告（這次沒有真實 y，所以只秀預測結果）
    print("\n" + "="*50)
    print("          PREDICTION ONLY MODE")
    print("="*50)
    print(f"輸入資料筆數       : {len(df):,}")
    print(f"使用特徵           : {features_used}")
    print(f"訓練時平均房價     : {train_mean_price:,.0f} 元")
    print(f"這批預測平均價格   : {preds.mean():,.0f} 元")
    print(f"預測價格範圍       : {preds.min():,.0f} ~ {preds.max():,.0f} 元")
    print("="*50)

    # 6. 匯出預測結果（這才是重點！）
    output_path = Path("data/processed/Prediction_Result_with_price.csv")
    output_path.parent.mkdir(parents=True, exist_ok=True)

    result_df = df[features_used].copy()
    result_df.insert(0, "id", range(1, len(result_df) + 1))
    result_df["predicted_price"] = preds.astype(int)

    result_df.to_csv(output_path, index=False, encoding="utf-8-sig")
    print(f"\n預測完成！結果已匯出")
    print(f"檔案路徑 → {output_path.resolve()}")
    print(f"共 {len(result_df)} 筆預測")


if __name__ == "__main__":
    # 自動判斷你要評估還是純預測
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--data", type=Path, default=Path("data/raw/TestDataForPrediction.csv"))
    args = parser.parse_args()

    evaluate(args.data)
