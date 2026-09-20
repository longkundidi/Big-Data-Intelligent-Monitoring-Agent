import argparse
import json
import os
import sys

import scipy.io as sio
import torch


DEPLOYMENT_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, DEPLOYMENT_DIR)

from inference import CLASS_NAMES, FaultDiagnoser  # noqa: E402


def load_samples(dataset_dir, class_name, frequency):
    path = os.path.join(dataset_dir, "%s_%s.mat" % (class_name, frequency))
    mat = sio.loadmat(path)
    key = next(key for key in mat if not key.startswith("__"))
    return mat[key]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--frequency", default="45")
    parser.add_argument("--limit", type=int, default=300)
    args = parser.parse_args()

    os.environ.setdefault(
        "MODEL_PATH", os.path.join(os.path.dirname(DEPLOYMENT_DIR), "model_saved.pth")
    )
    dataset_dir = os.path.join(
        os.path.dirname(DEPLOYMENT_DIR), "datasets", "%shz" % args.frequency
    )
    class_files = (
        "Normal",
        "Half",
        "All",
        "Oil",
        "Carbon",
        "Lowforce",
        "Gap",
        "Distance",
    )

    diagnoser = FaultDiagnoser()
    confusion = [[0 for _ in CLASS_NAMES] for _ in CLASS_NAMES]
    confidence_total = 0.0
    total = 0
    correct = 0

    for expected_code, class_file in enumerate(class_files):
        samples = load_samples(dataset_dir, class_file, args.frequency)[: args.limit]
        for sample in samples:
            result = diagnoser.predict(sample.tolist())
            predicted_code = result["fault_code"]
            confusion[expected_code][predicted_code] += 1
            confidence_total += result["confidence"]
            total += 1
            correct += int(predicted_code == expected_code)

    print(
        json.dumps(
            {
                "frequency_hz": int(args.frequency),
                "samples": total,
                "accuracy": correct / total,
                "mean_top1_confidence": confidence_total / total,
                "labels": [name for _, name in CLASS_NAMES],
                "confusion_matrix": confusion,
            },
            ensure_ascii=False,
            indent=2,
        )
    )


if __name__ == "__main__":
    main()
