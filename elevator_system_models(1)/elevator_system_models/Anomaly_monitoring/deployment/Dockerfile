FROM 192.168.16.211/algorithm/al_audit:0.0.6

WORKDIR /code

COPY deployment/model.py /code/model.py
COPY deployment/inference.py /code/inference.py
COPY deployment/main.py /code/main.py
COPY deployment/calibration.json /code/calibration.json
COPY model_saved.pth /code/model_saved.pth

ENV MODEL_PATH=/code/model_saved.pth \
    CALIBRATION_PATH=/code/calibration.json \
    TORCH_NUM_THREADS=2 \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1/healthz', timeout=3)" || exit 1

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "80", "--workers", "1"]
