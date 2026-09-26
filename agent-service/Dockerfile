FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

COPY pyproject.toml ./
COPY vendor ./vendor
RUN mkdir -p streamdoctor && touch streamdoctor/__init__.py
RUN pip install --index-url https://mirrors.aliyun.com/pypi/simple/ \
    --trusted-host mirrors.aliyun.com --no-cache-dir ./vendor/langchain-1.4.2-py3-none-any.whl && \
    pip install --index-url https://mirrors.aliyun.com/pypi/simple/ \
    --trusted-host mirrors.aliyun.com --no-cache-dir .

COPY streamdoctor ./streamdoctor
COPY topology.json runbooks.json ./
COPY replays ./replays

RUN mkdir -p /data

EXPOSE 8099

CMD ["uvicorn", "streamdoctor.api:app", "--host", "0.0.0.0", "--port", "8099"]
