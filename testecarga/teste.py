import requests
import time
import random
import threading
from collections import Counter
from concurrent.futures import ThreadPoolExecutor, as_completed

contador_global = 0 
lock = threading.Lock() 
URL = "http://localhost:8081/transacional/operacaoKafka"
TOTAL_REQUISICOES = 10000
THREADS = 10


status_counter = Counter()

CPF_FIXO = "cliente_1"
EMPRESA_FIXA = "empresa_1"

def gerar_pagamento(valor, numero):
    return {
        "valor": valor,
        "pagador": CPF_FIXO + str(numero),
        "beneficiario": EMPRESA_FIXA + str(numero)
    }

def fazer_requisicao(numero):
    global contador_global
    tentativas = 0
    valor_atual = round(random.uniform(50.0, 500.0), 2)
    pagamento_request = gerar_pagamento(valor_atual, numero)

    while True:
        with lock:
            if contador_global >= TOTAL_REQUISICOES:
                break
        tentativas += 1
        try:
            espera = random.uniform(0, 1)
            time.sleep(espera)

            response = requests.post(URL, json=pagamento_request, timeout=5)
            status_code = response.status_code
            status_counter[status_code] += 1
            with lock:
                contador_global += 1


            print(f"#{numero} | Tentativa {tentativas} | Cliente {pagamento_request['pagador']} | Valor R${pagamento_request['valor']:.2f} | {status_code} | Delay {espera:.1f}s | REQUEST {contador_global}")

            if status_code == 200:
                valor_atual = round(random.uniform(50.0, 500.0), 2)
                pagamento_request = gerar_pagamento(valor_atual, numero)
                tentativas = 0 

            elif status_code not in (202, 422):
                break

        except requests.exceptions.RequestException as e:
            status_counter['erro'] += 1
            print(f"#{numero}: ERRO - {str(e)}")
            break

def teste_de_carga():
    inicio = time.time()
    print(f"🔄 Iniciando teste com {TOTAL_REQUISICOES} requisições usando {THREADS} threads...\n")

    with ThreadPoolExecutor(max_workers=THREADS) as executor:
        futures = [executor.submit(fazer_requisicao, i) for i in range(TOTAL_REQUISICOES)]
        for future in as_completed(futures):
            pass

    fim = time.time()
    duracao = fim - inicio
    print(f"\n✅ Teste concluído em {duracao:.2f} segundos")

    print("\n📊 Estatísticas de status HTTP:")
    for status, count in status_counter.items():
        percentual = (count / sum(status_counter.values())) * 100
        print(f"  {status}: {count} respostas ({percentual:.1f}%)")

if __name__ == "__main__":
    teste_de_carga()
