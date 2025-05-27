from kafka import KafkaConsumer
import redis
import json
import logging
import os
import requests
import time


KAFKA_SERVER = os.environ.get("KAFKA_URL", "kafka1:19091")
KAFKA_TOPIC = os.environ.get("KAFKA_TOPIC", "topico_proativo")
REDIS_HOST = os.environ.get("REDIS_HOST", "localhost")
REDIS_PORT = int(os.environ.get("REDIS_PORT", 6379))
INTERMITENTE_URL = os.environ.get("INTERMITENTE_URL", "http://app-intermitente:8080/pagamentos/pagar")



logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

redis_client = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=True)

consumer = KafkaConsumer(
    KAFKA_TOPIC,
    bootstrap_servers=[KAFKA_SERVER],
    value_deserializer=lambda m: json.loads(m.decode('utf-8')),
    group_id="grupo_proativo",
    auto_offset_reset='earliest'
)


logging.info("📨 Consumidor Kafka iniciado. Aguardando mensagens...")


for message in consumer:
    try:
        payload = message.value
        logging.info(f"📥 Mensagem recebida: {payload}")

        documento_pagador = payload.get("pagador")
        documento_recebedor = payload.get("beneficiario")
        valor = payload.get("valor")

        if not (documento_pagador and documento_recebedor and valor):
            logging.warning("⚠️ Dados incompletos. Ignorando mensagem.")
            continue

        redis_key = f"pagamento:{documento_pagador}:{documento_recebedor}:{valor}"
        status_key = redis_key + ":status"
        response_key = redis_key + ":response"

        status = redis_client.get(status_key)

        if status == "FEITO":
            logging.info(f"✅ Já processado. Ignorando. {redis_key}")
            continue

        if status == "PENDENTE":
            logging.info(f"🔄 Já está sendo processado. Ignorando. {redis_key}")
            continue

        redis_client.set(status_key, "PENDENTE", ex=600)
        logging.info(f"🚀 Iniciando processamento para {redis_key}")

        try:
            response = requests.post(INTERMITENTE_URL, json=payload, timeout=10)
            response.raise_for_status()

            pagamento = response.json()

            redis_client.set(status_key, "FEITO", ex=3600)
            redis_client.set(response_key, json.dumps(pagamento), ex=3600)

            logging.info(f"✅ Pagamento processado e salvo no Redis. {redis_key}")

        except requests.exceptions.RequestException as e:
            logging.error(f"❌ Erro na chamada intermitente para {redis_key}: {e}")
            redis_client.set(status_key, "PENDENTE", ex=600) 

    except Exception as e:
        logging.error(f"❌ Erro geral no processamento da mensagem: {e}")
