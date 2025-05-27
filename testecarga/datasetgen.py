from prometheus_api_client import PrometheusConnect, MetricRangeDataFrame
import pandas as pd
from datetime import datetime, timedelta

prom = PrometheusConnect(url="http://localhost:9090", disable_ssl=True)

end_time = datetime.now()
start_time = end_time - timedelta(hours=24)

query = 'sum by (status) (http_server_requests_seconds_count{uri="/transacional/operacaoKafka"})'

metric_data = prom.custom_query_range(
    query=query,
    start_time=start_time,
    end_time=end_time,
    step='30s' 
)


df = pd.json_normalize(metric_data)


result = []

for entry in metric_data:
    status = entry['metric'].get('status', 'unknown')
    for value_pair in entry['values']:
        timestamp = pd.to_datetime(value_pair[0], unit='s')
        value = float(value_pair[1])
        result.append({'time': timestamp, 'status': status, 'value': value})

df_final = pd.DataFrame(result)


df_final.to_csv('dataset_operacaoKafka.csv', index=False)

print("✅ Dataset salvo com sucesso!")
