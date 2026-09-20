import math
import os
from typing import Dict, List

import torch

from model import FFCNet


WINDOW_SIZE = 1024
CLASS_NAMES = (
    ("normal", "正常"),
    ("brake_shoe_partial_wear", "闸瓦表面部分磨损"),
    ("brake_shoe_full_wear", "闸瓦表面全磨损"),
    ("oil_contamination", "闸瓦接触面存在油污"),
    ("foreign_matter", "闸瓦接触面存在异物"),
    ("insufficient_braking_force", "弹簧提供的制动力不足"),
    ("excessive_clearance", "闸瓦和制动轮间隙过大"),
    ("incomplete_contact", "闸瓦和制动轮未紧密贴合"),
)


class FaultDiagnoser:
    def __init__(self):
        self.model_path = os.getenv("MODEL_PATH", "/code/model_saved.pth")
        self.device = torch.device("cpu")
        torch.set_num_threads(int(os.getenv("TORCH_NUM_THREADS", "2")))
        torch.set_num_interop_threads(int(os.getenv("TORCH_INTEROP_THREADS", "1")))

        try:
            checkpoint = torch.load(
                self.model_path, map_location=self.device, weights_only=True
            )
        except TypeError:
            checkpoint = torch.load(self.model_path, map_location=self.device)
        if "model_state_dict" not in checkpoint:
            raise ValueError("Checkpoint does not contain model_state_dict")

        self.model = FFCNet(seq_length=WINDOW_SIZE).to(self.device)
        self.model.load_state_dict(checkpoint["model_state_dict"], strict=True)
        self.model.eval()

    def validate_values(self, values: List[float]) -> List[float]:
        if len(values) != WINDOW_SIZE:
            raise ValueError(
                "Input window must contain exactly 1024 values; got %d" % len(values)
            )
        validated = []
        for value in values:
            numeric_value = float(value)
            if not math.isfinite(numeric_value):
                raise ValueError("Input window contains NaN or infinity")
            validated.append(numeric_value)
        return validated

    def predict(self, values: List[float]) -> Dict[str, object]:
        validated = self.validate_values(values)
        tensor = torch.tensor(
            validated, dtype=torch.float32, device=self.device
        ).reshape(1, 1, WINDOW_SIZE)

        with torch.no_grad():
            logits = self.model(tensor)
            probabilities = torch.softmax(logits, dim=1)[0].cpu().tolist()

        fault_code = max(range(len(probabilities)), key=probabilities.__getitem__)
        fault_name_en, fault_name = CLASS_NAMES[fault_code]
        return {
            "sample_count": WINDOW_SIZE,
            "fault_code": fault_code,
            "fault_name": fault_name,
            "fault_name_en": fault_name_en,
            "is_fault": fault_code != 0,
            "confidence": round(probabilities[fault_code], 8),
            "probabilities": [
                {
                    "code": code,
                    "name": CLASS_NAMES[code][1],
                    "probability": round(probability, 8),
                }
                for code, probability in enumerate(probabilities)
            ],
            "model_version": "ffcnet-elevator-8class-v1",
        }
