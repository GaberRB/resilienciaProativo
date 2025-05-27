# Projeto de Resiliência e Proatividade com Erros e Ambientes Intermitentes

Este projeto foi desenvolvido para simular um ambiente **intermitente**, onde serviços podem falhar, reiniciar ou enfrentar desafios de conectividade. A ideia é testar e fortalecer a **resiliência** dos microsserviços.

## 🚀 Tecnologias Utilizadas
- **Kafka** + **Zookeeper**: Sistema de mensageria distribuída
- **Redis**: Banco de dados em memória
- **Prometheus** + **Grafana**: Monitoramento de métricas
- **Kafdrop**: Interface para visualizar tópicos do Kafka
- **Microsserviços**:
  - `intermitente-server`
  - `proativo-server`
  - `transacional-server`

## 🛠️ Pré-requisitos
Certifique-se de que possui:
- **Docker** instalado ([como instalar](https://docs.docker.com/get-docker/))
- **Docker Compose** configurado corretamente

## 📦 Como Rodar o Projeto
### 1️⃣ Clone o repositório
```sh
git clone https://github.com/seu-usuario/projeto-resiliencia.git
cd projeto-resiliencia


2️⃣ Inicie os serviços com Docker Compose
    docker-compose up -d
Isso iniciará todos os containers em segundo plano.

3️⃣ Verifique se os containers estão rodando
docker ps


🔍 Monitoramento
- Kafdrop (Interface Kafka): http://localhost:9000
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
🛠️ Comandos Úteis
- Parar os serviços:
docker-compose down
- Reiniciar os serviços:
docker-compose restart
- Ver logs de erros:
docker logs transacional-server
docker logs redis
docker logs kafka1


🐛 Solução de Problemas
Caso ocorra um erro de conexão com Redis ou Kafka:
docker-compose restart redis kafka1 transacional-server


Se o Redis não estiver respondendo, teste manualmente:
docker exec -it redis redis-cli ping


Se a resposta for PONG, o Redis está funcionando corretamente.
