FROM python:3.11-slim

WORKDIR /app

COPY . .

RUN pip install --upgrade pip && \
    pip install kafka-python redis requests

CMD ["python", "proativo.py"]
