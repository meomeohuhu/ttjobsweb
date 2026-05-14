import json
from pathlib import Path


NOTEBOOK = Path("notebook231d637d95.ipynb")


def md(text):
    return {
        "cell_type": "markdown",
        "metadata": {},
        "source": [line + "\n" for line in text.strip().split("\n")],
    }


def code(text):
    return {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [line + "\n" for line in text.strip().split("\n")],
    }


cells = [
    md("""
# TTJobs CV-JD Matching Model Training

Notebook này dùng để train model matching cho TTJobs.

Mục tiêu:
- Nhận input gồm `CV + JD`.
- Dự đoán 3 nhãn: `not_match`, `partial_match`, `match`.
- Train bằng dataset tiếng Việt synthetic.
- Tự động dùng thêm `job_resume_fit` nếu file này có trong Kaggle input.
- Chỉ dùng `Resume.csv` để tham khảo phân bố ngành, không dùng trực tiếp cho matching vì file đó không có JD.

Lưu ý: synthetic data chỉ phù hợp để bootstrap/demo. Khi đánh giá nghiêm túc cần có test set từ dữ liệu thật.
"""),
    md("""
## 1. Cài thư viện

Kaggle thường có sẵn nhiều package, nhưng cell này đảm bảo đủ thư viện cho Hugging Face Trainer, metric và train/test split.
"""),
    code("""
!pip install -q transformers datasets evaluate accelerate scikit-learn
"""),
    md("""
## 2. Import và cấu hình

Cell này định nghĩa model, label mapping, seed và thư mục output. Khi deploy, `id2label` và `label2id` phải được lưu cùng model để API không trả về `LABEL_0`, `LABEL_1`.
"""),
    code("""
import json
import random
from pathlib import Path
from datetime import datetime

import numpy as np
import pandas as pd
import torch
import evaluate
from datasets import Dataset
from sklearn.metrics import classification_report, confusion_matrix
from sklearn.model_selection import train_test_split
from transformers import AutoModelForSequenceClassification, AutoTokenizer, Trainer, TrainingArguments

# Cố định seed để kết quả ổn định hơn giữa các lần chạy.
SEED = 42
random.seed(SEED)
np.random.seed(SEED)
torch.manual_seed(SEED)

# Baseline multilingual cho bài toán classification CV-JD.
MODEL_NAME = "distilbert-base-multilingual-cased"
DATA_ROOT = Path("/kaggle/input")
OUTPUT_DIR = Path("/kaggle/working/ttjobs-matching-model")

LABEL2ID = {"not_match": 0, "partial_match": 1, "match": 2}
ID2LABEL = {v: k for k, v in LABEL2ID.items()}

print("Model:", MODEL_NAME)
print("Output:", OUTPUT_DIR)
"""),
    md("""
## 3. Helper đọc và chuẩn hóa dữ liệu

Các hàm dưới đây giúp notebook ít phụ thuộc vào đường dẫn cụ thể trên Kaggle:
- Tìm file theo tên trong `/kaggle/input`.
- Đọc JSONL UTF-8.
- Quy đổi score thành label.
- Chuẩn hóa text để tránh lỗi `NaN` hoặc khoảng trắng thừa.
"""),
    code("""
def find_input_file(filename: str):
    \"\"\"Tìm file theo tên trong /kaggle/input.\"\"\"
    matches = list(DATA_ROOT.rglob(filename))
    return matches[0] if matches else None


def read_jsonl(path: Path):
    \"\"\"Đọc JSONL, mỗi dòng là một JSON object.\"\"\"
    rows = []
    with path.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                rows.append(json.loads(line))
    return pd.DataFrame(rows)


def score_to_label(score):
    \"\"\"Quy đổi score 0-100 thành 3 mức matching.\"\"\"
    if pd.isna(score):
        return None
    score = float(score)
    if score >= 75:
        return "match"
    if score >= 45:
        return "partial_match"
    return "not_match"


def normalize_text(value):
    \"\"\"Chuẩn hóa text đơn giản trước khi đưa vào tokenizer.\"\"\"
    if pd.isna(value):
        return ""
    return " ".join(str(value).split())
"""),
    md("""
## 4. Load dataset

Notebook bắt buộc cần `vietnamese_cv_jd_pairs_seed.jsonl`. Nếu có `Resume.csv`, notebook chỉ in thống kê để tham khảo. `Resume.csv` không được đưa vào train matching vì thiếu cặp JD.
"""),
    code("""
vi_path = find_input_file("vietnamese_cv_jd_pairs_seed.jsonl")
resume_path = find_input_file("Resume.csv")

if vi_path is None:
    raise FileNotFoundError("Không tìm thấy vietnamese_cv_jd_pairs_seed.jsonl trong /kaggle/input")

vi_df = read_jsonl(vi_path)
print("Vietnamese synthetic path:", vi_path)
print("Vietnamese synthetic shape:", vi_df.shape)
print(vi_df[["industry", "label", "score"]].head())

if resume_path is not None:
    resume_df = pd.read_csv(resume_path)
    print("Resume.csv path:", resume_path)
    print("Resume.csv shape:", resume_df.shape)
    if "Category" in resume_df.columns:
        print("Resume category sample:")
        print(resume_df["Category"].value_counts().head())
else:
    print("Không tìm thấy Resume.csv, bỏ qua thống kê CV category.")
"""),
    md("""
## 5. Chuẩn hóa Vietnamese synthetic dataset

Mỗi record training cần có:
- `cv`: nội dung CV.
- `jd`: mô tả công việc.
- `label`: nhãn matching.
- `industry`: ngành.
- `score`: điểm 0-100 để tham khảo.

Model sẽ học từ chuỗi `CV: ... JD: ...`.
"""),
    code("""
required_cols = {"cv", "jd", "label", "score", "industry"}
missing_cols = required_cols - set(vi_df.columns)
if missing_cols:
    raise ValueError(f"Vietnamese dataset thiếu cột: {missing_cols}")

vi_match_df = vi_df.copy()
vi_match_df["cv"] = vi_match_df["cv"].map(normalize_text)
vi_match_df["jd"] = vi_match_df["jd"].map(normalize_text)
vi_match_df["label"] = vi_match_df["label"].map(normalize_text)
vi_match_df["source"] = vi_match_df.get("source", "synthetic_seed_v1")
vi_match_df["label_id"] = vi_match_df["label"].map(LABEL2ID)

# Bỏ các dòng label không hợp lệ nếu có.
vi_match_df = vi_match_df.dropna(subset=["label_id", "cv", "jd"]).copy()
vi_match_df["label_id"] = vi_match_df["label_id"].astype(int)
vi_match_df["text"] = "CV: " + vi_match_df["cv"] + "\\nJD: " + vi_match_df["jd"]

print("Vietnamese matching rows:", len(vi_match_df))
print(vi_match_df["label"].value_counts())
print("Industry count:", vi_match_df["industry"].nunique())
"""),
    md("""
## 6. Optional: load thêm `job_resume_fit`

Nếu bạn upload thêm file có tên chứa `job_resume_fit`, notebook sẽ tự thử đọc. Vì mỗi dataset public có schema khác nhau, code sẽ tự đoán cột resume, JD, label hoặc score. Nếu không đủ cột thì bỏ qua an toàn.
"""),
    code("""
def find_job_resume_fit_file():
    for path in DATA_ROOT.rglob("*"):
        if path.is_file() and "job_resume_fit" in path.name.lower() and path.suffix.lower() in {".csv", ".json", ".jsonl"}:
            return path
    return None


def first_existing(columns, keywords):
    \"\"\"Tìm cột đầu tiên có tên chứa một keyword mong muốn.\"\"\"
    lower_map = {c.lower(): c for c in columns}
    for keyword in keywords:
        for lower_name, original_name in lower_map.items():
            if keyword in lower_name:
                return original_name
    return None


def load_job_resume_fit(path: Path):
    \"\"\"Load job_resume_fit với schema linh hoạt.\"\"\"
    if path.suffix.lower() == ".csv":
        df = pd.read_csv(path)
    elif path.suffix.lower() == ".jsonl":
        df = read_jsonl(path)
    else:
        df = pd.read_json(path)

    resume_col = first_existing(df.columns, ["resume", "cv"])
    job_col = first_existing(df.columns, ["job_description", "job description", "jd", "job", "posting"])
    label_col = first_existing(df.columns, ["label", "fit_label", "match_label"])
    score_col = first_existing(df.columns, ["match_score", "fit_score", "score", "similarity"])

    if resume_col is None or job_col is None:
        print("job_resume_fit thiếu cột resume/job, bỏ qua. Columns:", list(df.columns))
        return pd.DataFrame()

    out = pd.DataFrame()
    out["cv"] = df[resume_col].map(normalize_text)
    out["jd"] = df[job_col].map(normalize_text)

    if label_col is not None:
        raw_label = df[label_col].astype(str).str.lower().str.strip()
        label_map = {
            "0": "not_match", "1": "partial_match", "2": "match",
            "not_fit": "not_match", "not match": "not_match", "not_match": "not_match",
            "partial": "partial_match", "partial_match": "partial_match", "maybe": "partial_match",
            "fit": "match", "match": "match", "matched": "match",
        }
        out["label"] = raw_label.map(label_map).fillna(raw_label)
    elif score_col is not None:
        out["score"] = pd.to_numeric(df[score_col], errors="coerce")
        if out["score"].max() <= 1.0:
            out["score"] = out["score"] * 100
        out["label"] = out["score"].map(score_to_label)
    else:
        print("job_resume_fit không có label/score, bỏ qua.")
        return pd.DataFrame()

    out["score"] = out.get("score", np.nan)
    out["industry"] = "UNKNOWN"
    out["source"] = "job_resume_fit"
    out["label_id"] = out["label"].map(LABEL2ID)
    out = out.dropna(subset=["label_id", "cv", "jd"]).copy()
    out["label_id"] = out["label_id"].astype(int)
    out["text"] = "CV: " + out["cv"] + "\\nJD: " + out["jd"]
    return out


job_fit_df = pd.DataFrame()
job_fit_path = find_job_resume_fit_file()
if job_fit_path is not None:
    print("job_resume_fit path:", job_fit_path)
    job_fit_df = load_job_resume_fit(job_fit_path)
    print("job_resume_fit normalized rows:", len(job_fit_df))
else:
    print("Không tìm thấy job_resume_fit, notebook sẽ train bằng Vietnamese synthetic seed.")
"""),
    md("""
## 7. Gộp dữ liệu training

Dữ liệu chính là `CV + JD + label`. Nếu có `job_resume_fit`, notebook gộp thêm vào synthetic data. Sau đó loại duplicate để tránh train/test quá giống nhau.
"""),
    code("""
columns = ["text", "label_id", "label", "industry", "source", "score", "cv", "jd"]
frames = [vi_match_df[columns]]
if len(job_fit_df) > 0:
    frames.append(job_fit_df[columns])

match_df = pd.concat(frames, ignore_index=True)
match_df = match_df.drop_duplicates(subset=["text", "label_id"]).reset_index(drop=True)

print("Total matching rows:", len(match_df))
print("Label distribution:")
print(match_df["label"].value_counts())
print("Source distribution:")
print(match_df["source"].value_counts())
"""),
    md("""
## 8. Train/validation/test split

Không đánh giá trên chính data train. Notebook tách:
- 70% train
- 15% validation
- 15% test

Split được stratify theo `label + industry` nếu nhóm đó đủ mẫu, giúp các ngành và nhãn phân bố đều hơn.
"""),
    code("""
match_df["stratify_key"] = match_df["label"] + "__" + match_df["industry"].astype(str)
key_counts = match_df["stratify_key"].value_counts()
match_df.loc[match_df["stratify_key"].map(key_counts) < 3, "stratify_key"] = match_df["label"]

train_df, temp_df = train_test_split(
    match_df,
    test_size=0.30,
    random_state=SEED,
    stratify=match_df["stratify_key"],
)

valid_df, test_df = train_test_split(
    temp_df,
    test_size=0.50,
    random_state=SEED,
    stratify=temp_df["label"],
)

print("train:", train_df.shape, train_df["label"].value_counts().to_dict())
print("valid:", valid_df.shape, valid_df["label"].value_counts().to_dict())
print("test :", test_df.shape, test_df["label"].value_counts().to_dict())
"""),
    md("""
## 9. Tokenize

Tokenizer biến text thành token ids cho model. `max_length=512` là giới hạn phổ biến của BERT/DistilBERT.
"""),
    code("""
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)


def to_hf_dataset(df):
    return Dataset.from_pandas(
        df[["text", "label_id"]].rename(columns={"label_id": "labels"}),
        preserve_index=False,
    )


def tokenize(batch):
    return tokenizer(
        batch["text"],
        truncation=True,
        padding="max_length",
        max_length=512,
    )


hf_dataset = {
    "train": to_hf_dataset(train_df),
    "valid": to_hf_dataset(valid_df),
    "test": to_hf_dataset(test_df),
}

tokenized = {name: ds.map(tokenize, batched=True) for name, ds in hf_dataset.items()}
print(tokenized)
"""),
    md("""
## 10. Khởi tạo model

`id2label` và `label2id` được set trực tiếp vào model config để lúc deploy API trả về label dễ hiểu.
"""),
    code("""
model = AutoModelForSequenceClassification.from_pretrained(
    MODEL_NAME,
    num_labels=3,
    id2label=ID2LABEL,
    label2id=LABEL2ID,
)

model.config.id2label = ID2LABEL
model.config.label2id = LABEL2ID
print(model.config.id2label)
"""),
    md("""
## 11. Metric

Metric chính nên theo dõi là `macro_f1`, vì nó cân bằng giữa 3 nhãn. Accuracy có thể đẹp giả nếu một nhãn chiếm đa số.
"""),
    code("""
accuracy_metric = evaluate.load("accuracy")
f1_metric = evaluate.load("f1")


def compute_metrics(eval_pred):
    logits, labels = eval_pred
    preds = np.argmax(logits, axis=-1)
    return {
        "accuracy": accuracy_metric.compute(predictions=preds, references=labels)["accuracy"],
        "macro_f1": f1_metric.compute(predictions=preds, references=labels, average="macro")["f1"],
        "weighted_f1": f1_metric.compute(predictions=preds, references=labels, average="weighted")["f1"],
    }
"""),
    md("""
## 12. Train model

Notebook chọn checkpoint tốt nhất theo `macro_f1` trên validation set. Đây là cách hợp lý hơn chọn theo loss hoặc accuracy cho bài toán 3 nhãn.
"""),
    code("""
args = TrainingArguments(
    output_dir=str(OUTPUT_DIR),
    eval_strategy="epoch",
    save_strategy="epoch",
    learning_rate=2e-5,
    per_device_train_batch_size=16,
    per_device_eval_batch_size=16,
    num_train_epochs=3,
    weight_decay=0.01,
    load_best_model_at_end=True,
    metric_for_best_model="macro_f1",
    greater_is_better=True,
    report_to="none",
    seed=SEED,
)

trainer = Trainer(
    model=model,
    args=args,
    train_dataset=tokenized["train"],
    eval_dataset=tokenized["valid"],
    tokenizer=tokenizer,
    compute_metrics=compute_metrics,
)

trainer.train()
valid_metrics = trainer.evaluate(tokenized["valid"])
print(valid_metrics)
"""),
    md("""
## 13. Đánh giá trên test set

Cell này in metric, classification report và confusion matrix. Confusion matrix giúp biết model đang nhầm `partial_match` với `match` hay `not_match`.
"""),
    code("""
test_output = trainer.predict(tokenized["test"])
test_logits = test_output.predictions
test_labels = test_output.label_ids
test_preds = np.argmax(test_logits, axis=-1)

print("Test metrics:")
print(compute_metrics((test_logits, test_labels)))

label_names = [ID2LABEL[i] for i in range(len(ID2LABEL))]
print("\\nClassification report:")
print(classification_report(test_labels, test_preds, target_names=label_names, digits=4))

cm = confusion_matrix(test_labels, test_preds, labels=list(ID2LABEL.keys()))
cm_df = pd.DataFrame(cm, index=[f"true_{x}" for x in label_names], columns=[f"pred_{x}" for x in label_names])
cm_df
"""),
    md("""
## 14. Đánh giá riêng theo source

Nếu notebook có cả synthetic và `job_resume_fit`, cần xem metric riêng từng source. Metric tổng có thể cao nhưng source dữ liệu thật vẫn yếu.
"""),
    code("""
def evaluate_subset_by_source(source_name):
    subset = test_df[test_df["source"] == source_name]
    if subset.empty:
        print(f"Skip {source_name}: no rows in test set")
        return None
    ds = to_hf_dataset(subset).map(tokenize, batched=True)
    pred = trainer.predict(ds)
    metrics = compute_metrics((pred.predictions, pred.label_ids))
    print(f"Source={source_name}, rows={len(subset)}", metrics)
    return metrics


source_metrics = {}
for source_name in sorted(test_df["source"].unique()):
    source_metrics[source_name] = evaluate_subset_by_source(source_name)
"""),
    md("""
## 15. Save model và metadata

Các file trong `/kaggle/working/ttjobs-matching-model` cần tải về để đưa vào `ai-service`.
"""),
    code("""
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
trainer.save_model(str(OUTPUT_DIR))
tokenizer.save_pretrained(str(OUTPUT_DIR))

label_config = {
    "id2label": ID2LABEL,
    "label2id": LABEL2ID,
}
with (OUTPUT_DIR / "label_mapping.json").open("w", encoding="utf-8") as f:
    json.dump(label_config, f, ensure_ascii=False, indent=2)

metadata = {
    "model_name": MODEL_NAME,
    "created_at": datetime.utcnow().isoformat() + "Z",
    "seed": SEED,
    "labels": label_config,
    "rows": {
        "total": int(len(match_df)),
        "train": int(len(train_df)),
        "valid": int(len(valid_df)),
        "test": int(len(test_df)),
    },
    "label_distribution": match_df["label"].value_counts().to_dict(),
    "source_distribution": match_df["source"].value_counts().to_dict(),
    "industry_count": int(match_df["industry"].nunique()),
    "test_metrics": compute_metrics((test_logits, test_labels)),
    "source_metrics": source_metrics,
}
with (OUTPUT_DIR / "training_metadata.json").open("w", encoding="utf-8") as f:
    json.dump(metadata, f, ensure_ascii=False, indent=2)

print("Saved to:", OUTPUT_DIR)
print(list(OUTPUT_DIR.iterdir()))
"""),
    md("""
## 16. Quick inference test

Cell cuối test nhanh một cặp CV-JD mới để đảm bảo model trả label đúng format trước khi tải về deploy.
"""),
    code("""
def predict_match(cv, jd):
    text = "CV: " + normalize_text(cv) + "\\nJD: " + normalize_text(jd)
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True, max_length=512)
    inputs = {k: v.to(model.device) for k, v in inputs.items()}
    model.eval()
    with torch.no_grad():
        logits = model(**inputs).logits[0]
        probs = torch.softmax(logits, dim=-1).detach().cpu().numpy()
    pred_id = int(np.argmax(probs))
    return {
        "label": ID2LABEL[pred_id],
        "confidence": float(probs[pred_id]),
        "probabilities": {ID2LABEL[i]: float(probs[i]) for i in range(len(probs))},
    }


sample_cv = "Ứng viên backend Java 4 năm kinh nghiệm, Spring Boot, PostgreSQL, Redis, Docker và CI/CD."
sample_jd = "Tuyển Java Backend Developer xây REST API, Spring Boot, PostgreSQL, Redis và triển khai Docker."
predict_match(sample_cv, sample_jd)
"""),
]


notebook = {
    "cells": cells,
    "metadata": {
        "kernelspec": {
            "display_name": "Python 3",
            "language": "python",
            "name": "python3",
        },
        "language_info": {
            "name": "python",
            "pygments_lexer": "ipython3",
        },
    },
    "nbformat": 4,
    "nbformat_minor": 4,
}

NOTEBOOK.write_text(json.dumps(notebook, ensure_ascii=False, indent=1), encoding="utf-8")
print(f"Rewrote {NOTEBOOK} with {len(cells)} cells")
