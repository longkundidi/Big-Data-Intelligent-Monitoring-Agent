import json
import math
import os
from typing import Dict, List

import torch

from model import MaskedEPTNet


WINDOW_SIZE = 1024


class AnomalyMonitor:
    def __init__(self):
        self.model_path = os.getenv("MODEL_PATH", "/code/model_saved.pth")
        self.calibration_path = os.getenv("CALIBRATION_PATH", "/code/calibration.json")
        self.device = torch.device("cpu")
        torch.set_num_threads(int(os.getenv("TORCH_NUM_THREADS", "2")))

        with open(self.calibration_path, "r", encoding="utf-8") as calibration_file:
            self.calibration = json.load(calibration_file)
        if self.calibration.get("input_size") != WINDOW_SIZE:
            raise ValueError("Calibration input_size must be 1024")

        checkpoint = torch.load(self.model_path, map_location=self.device)
        self.model = MaskedEPTNet(seq_len=WINDOW_SIZE).to(self.device)
        self.model.load_state_dict(checkpoint["model_state_dict"])
        self.model.eval()

    def predict(self, values: List[float]) -> Dict[str, object]:
        if len(values) != WINDOW_SIZE:
            raise ValueError("Input window must contain exactly 1024 values")
        if not all(math.isfinite(value) for value in values):
            raise ValueError("Input window contains NaN or infinity")

        tensor = torch.tensor(values, dtype=torch.float32, device=self.device).reshape(1, 1, WINDOW_SIZE)
        with torch.no_grad():
            _, prediction, target, _ = self.model(tensor, mask_ratio=0.0)
            raw_error = ((prediction - target) ** 2).mean(dim=(1, 2)).item()

        mean = float(self.calibration["raw_score_mean"])
        std = float(self.calibration["raw_score_std"])
        threshold = float(self.calibration["threshold"])
        anomaly_score = abs(raw_error - mean) / std
        is_anomaly = anomaly_score > threshold

        return {
            "sample_count": WINDOW_SIZE,
            "status": "abnormal" if is_anomaly else "normal",
            "is_anomaly": is_anomaly,
            "anomaly_score": round(anomaly_score, 8),
            "threshold": round(threshold, 8),
            "raw_reconstruction_error": round(raw_error, 12),
            "calibration_version": self.calibration["version"],
        }
