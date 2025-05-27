from fastapi import FastAPI
import pandas as pd
from datetime import datetime
from prophet import Prophet

app = FastAPI()

print("🚀 Inicializando API com previsão por minuto...")

# 🚩 Carregar dados históricos
df = pd.read_csv('dataset_operacaoKafka.csv')

# ✅ Converter status para string e tempo para datetime
df['status'] = df['status'].astype(str)
df['time'] = pd.to_datetime(df['time'])

# 🔥 Filtra registros de erro (status diferente de 200)
df_erros = df[df['status'] == '422']

# ⏱️ Agrupa por minuto
df_erros = (
    df_erros.groupby(pd.Grouper(key='time', freq='min'))
    .sum(numeric_only=True)
    .reset_index()
)

# 🛑 Se não houver dados de erro, cria um dummy
if df_erros.empty:
    df_erros = pd.DataFrame({'ds': [pd.Timestamp.now()], 'y': [0]})
else:
    df_erros = df_erros.rename(columns={'time': 'ds', 'value': 'y'})

# 🧠 Treinamento do modelo Prophet
model = Prophet(daily_seasonality=True, weekly_seasonality=True, yearly_seasonality=False)
model.fit(df_erros)

# 🔮 Gera previsão para os próximos 180 minutos (3 horas)
future = model.make_future_dataframe(periods=60, freq='min')
forecast = model.predict(future)

print("✅ IA treinada e pronta para previsão por minuto!")

# 🚀 Função para buscar melhores e piores horários históricos
def melhores_piores_horarios():
    df_erros_copy = df_erros.copy()
    df_erros_copy['hora'] = df_erros_copy['ds'].dt.strftime('%H:%M')

    agrupado = (
        df_erros_copy.groupby('hora')
        .agg(total_erros=('y', 'sum'))
        .reset_index()
        .sort_values('total_erros')
    )

    melhores = agrupado.head(3).to_dict(orient='records')
    piores = agrupado.tail(3).to_dict(orient='records')

    return melhores, piores


# =========================
# 🚀 Rotas da API
# =========================

@app.get("/")
def read_root():
    return {"msg": "🔍 API IA - Verifica se AGORA é um bom momento para pagar (Previsão por Minuto)"}


@app.get("/verificar_pagamento")
def verificar_pagamento():
    # 🔥 Hora atual arredondada para o minuto
    hora_atual = pd.Timestamp.now().floor('min')

    dados_forecast = forecast[forecast['ds'] == hora_atual]

    melhores, piores = melhores_piores_horarios()

    if dados_forecast.empty:
        return {
            "melhor_momento": None,
            "mensagem": f"⚠️ Sem previsão disponível para {hora_atual}.",
            "hora_consultada": str(hora_atual),
            "melhores_horarios_para_pagar": melhores,
            "piores_horarios_para_pagar": piores
        }

    risco = dados_forecast['yhat'].values[0]
    limite = df_erros['y'].mean() + df_erros['y'].std()

    if risco <= limite:
        return {
            "melhor_momento": True,
            "mensagem": "✅ IA prevê que é um bom momento para pagar.",
            "hora_consultada": str(hora_atual),
            "risco_previsto": round(risco, 2),
            "limite_aceitavel": round(limite, 2),
            "melhores_horarios_para_pagar": melhores,
            "piores_horarios_para_pagar": piores
        }
    else:
        return {
            "melhor_momento": False,
            "mensagem": "🚫 IA prevê que esse horário possui alto risco de falha.",
            "hora_consultada": str(hora_atual),
            "risco_previsto": round(risco, 2),
            "limite_aceitavel": round(limite, 2),
            "melhores_horarios_para_pagar": melhores,
            "piores_horarios_para_pagar": piores
        }


@app.get("/previsao")
def get_previsao():
    resultado = forecast[['ds', 'yhat', 'yhat_lower', 'yhat_upper']].copy()
    resultado['ds'] = resultado['ds'].astype(str)  # Converte datetime para string

    previsoes_formatadas = []

    for _, row in resultado.iterrows():
        previsoes_formatadas.append({
            "horario": row['ds'],
            "risco_previsto": round(row['yhat'], 2),
            "risco_minimo_estimado": round(row['yhat_lower'], 2),
            "risco_maximo_estimado": round(row['yhat_upper'], 2),
            "descricao": traduzir_risco(row['yhat'])
        })

    return previsoes_formatadas


def traduzir_risco(risco):
    if risco <= 1:
        return "🟢 Baixo risco de falhas - Ótimo momento para pagar"
    elif risco <= 3:
        return "🟡 Risco moderado - Possíveis lentidões"
    else:
        return "🔴 Alto risco de falhas - Evite este horário"
