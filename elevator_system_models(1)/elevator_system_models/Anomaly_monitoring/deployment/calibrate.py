import argparse
import hashlib
import json
import os
from datetime import datetime, timezone

import numpy as np
import scipy.io as sio
import torch
from sklearn.metrics import accuracy_score, f1_score, precision_score, recall_score, roc_auc_score, roc_curve

from model import MaskedEPTNet


FAULT_NAMES = ["Distance", "All", "Lowforce", "Half", "Carbon", "Gap", "Oil"]
WINDOW_SIZE = 1024


def load_mat(path):
    mat = sio.loadmat(path)
    keys = [key for key in mat.keys() if not key.startswith("__")]
    if len(keys) != 1:
        raise ValueError("Expected one data variable in %s" % path)
    data = np.asarray(mat[keys[0]], dtype=np.float32)
    if data.ndim != 2:
        raise ValueError("Expected a two-dimensional matrix in %s" % path)
    if data.shape[1] != WINDOW_SIZE and data.shape[0] == WINDOW_SIZE:
        data = data.T
    if data.shape[1] != WINDOW_SIZE:
        raise ValueError("Expected shape [N, 1024] in %s; got %s" % (path, data.shape))
    if not np.isfinite(data).all():
        raise ValueError("Dataset contains NaN or infinity: %s" % path)
    return data


def reconstruction_scores(model, data, batch_size):
    scores = []
    with torch.no_grad():
        for offset in range(0, len(data), batch_size):
            batch = torch.from_numpy(data[offset : offset + batch_size]).float().unsqueeze(1)
            _, prediction, target, _ = model(batch, mask_ratio=0.0)
            batch_scores = ((prediction - target) ** 2).mean(dim=(1, 2))
            scores.extend(batch_scores.cpu().numpy().tolist())
    return np.asarray(scores, dtype=np.float64)


def sha256(path):
    digest = hashlib.sha256()
    with open(path, "rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", required=True)
    parser.add_argument("--data-dir", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--batch-size", type=int, default=128)
    args = parser.parse_args()

    torch.set_num_threads(int(os.getenv("TORCH_NUM_THREADS", "4")))
    checkpoint = torch.load(args.model, map_location="cpu")
    model = MaskedEPTNet(seq_len=WINDOW_SIZE)
    model.load_state_dict(checkpoint["model_state_dict"])
    model.eval()

    normal = load_mat(os.path.join(args.data_dir, "Normal_45.mat"))
    normal_raw = reconstruction_scores(model, normal, args.batch_size)
    raw_mean = float(normal_raw.mean())
    raw_std = float(normal_raw.std() + 1e-8)
    normal_scores = np.abs(normal_raw - raw_mean) / raw_std

    fault_scores = {}
    combined_fault_scores = []
    for fault_name in FAULT_NAMES:
        fault_data = load_mat(os.path.join(args.data_dir, "%s_45.mat" % fault_name))
        raw_scores = reconstruction_scores(model, fault_data, args.batch_size)
        scores = np.abs(raw_scores - raw_mean) / raw_std
        fault_scores[fault_name] = scores
        combined_fault_scores.append(scores)

    all_fault_scores = np.concatenate(combined_fault_scores)
    labels = np.concatenate(
        [np.zeros(len(normal_scores), dtype=np.int64), np.ones(len(all_fault_scores), dtype=np.int64)]
    )
    scores = np.concatenate([normal_scores, all_fault_scores])
    false_positive_rate, true_positive_rate, thresholds = roc_curve(labels, scores)
    best_index = int(np.argmax(true_positive_rate - false_positive_rate))
    threshold = float(thresholds[best_index])
    predictions = (scores > threshold).astype(np.int64)

    per_fault = {}
    for fault_name, current_scores in fault_scores.items():
        current_labels = np.concatenate(
            [np.zeros(len(normal_scores), dtype=np.int64), np.ones(len(current_scores), dtype=np.int64)]
        )
        current_all_scores = np.concatenate([normal_scores, current_scores])
        current_predictions = (current_all_scores > threshold).astype(np.int64)
        per_fault[fault_name] = {
            "sample_count": int(len(current_scores)),
            "auc": float(roc_auc_score(current_labels, current_all_scores)),
            "f1": float(f1_score(current_labels, current_predictions, zero_division=0)),
            "recall": float(recall_score(current_labels, current_predictions, zero_division=0)),
        }

    calibration = {
        "version": "masked-eptnet-45hz-v1",
        "created_at": datetime.now(timezone.utc).isoformat(),
        "input_size": WINDOW_SIZE,
        "raw_score_mean": raw_mean,
        "raw_score_std": raw_std,
        "threshold": threshold,
        "comparison": ">",
        "model_sha256": sha256(args.model),
        "calibration_source": "45hz labeled datasets; normal plus seven fault classes",
        "sample_counts": {
            "normal": int(len(normal_scores)),
            "fault": int(len(all_fault_scores)),
        },
        "metrics": {
            "auc": float(roc_auc_score(labels, scores)),
            "accuracy": float(accuracy_score(labels, predictions)),
            "precision": float(precision_score(labels, predictions, zero_division=0)),
            "recall": float(recall_score(labels, predictions, zero_division=0)),
            "f1": float(f1_score(labels, predictions, zero_division=0)),
            "per_fault": per_fault,
        },
    }

    with open(args.output, "w", encoding="utf-8") as output_file:
        json.dump(calibration, output_file, ensure_ascii=False, indent=2)
        output_file.write("\n")
    print(json.dumps(calibration, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
