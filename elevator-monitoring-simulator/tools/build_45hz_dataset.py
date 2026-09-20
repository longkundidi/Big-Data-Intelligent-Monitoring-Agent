from pathlib import Path
import gzip
import struct

import numpy as np
from scipy.io import loadmat


CLASSES = [
    "Normal",
    "Half",
    "All",
    "Oil",
    "Carbon",
    "Lowforce",
    "Gap",
    "Distance",
]
SAMPLE_COUNT = 300
WINDOW_SIZE = 1024
MAGIC = b"ELEV45V1"


def main():
    module_root = Path(__file__).resolve().parents[1]
    workspace_root = module_root.parent
    source_root = (
        workspace_root
        / "elevator_system_models(1)"
        / "elevator_system_models"
        / "Fault_diagnosis"
        / "datasets"
        / "45hz"
    )
    output = (
        module_root
        / "src"
        / "main"
        / "resources"
        / "datasets"
        / "elevator-45hz.bin.gz"
    )
    output.parent.mkdir(parents=True, exist_ok=True)

    with output.open("wb") as raw_output:
        with gzip.GzipFile(fileobj=raw_output, mode="wb", mtime=0) as compressed:
            compressed.write(MAGIC)
            compressed.write(struct.pack(">iii", len(CLASSES), SAMPLE_COUNT, WINDOW_SIZE))
            for label, class_code in enumerate(CLASSES):
                mat_path = source_root / f"{class_code}_45.mat"
                mat = loadmat(mat_path)
                keys = [key for key in mat if not key.startswith("__")]
                if len(keys) != 1:
                    raise ValueError(f"Expected one data array in {mat_path}; got {keys}")
                samples = np.asarray(mat[keys[0]])
                if samples.shape == (WINDOW_SIZE, SAMPLE_COUNT):
                    samples = samples.T
                if samples.shape != (SAMPLE_COUNT, WINDOW_SIZE):
                    raise ValueError(
                        f"Expected {(SAMPLE_COUNT, WINDOW_SIZE)} in {mat_path}; got {samples.shape}"
                    )
                if not np.isfinite(samples).all():
                    raise ValueError(f"Non-finite value found in {mat_path}")
                compressed.write(struct.pack(">i", label))
                compressed.write(samples.astype(">f4", copy=False).tobytes(order="C"))

    print(f"Wrote {output} ({output.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
