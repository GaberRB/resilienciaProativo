#!/bin/sh
echo "⏳ Aguardando Redis em $SPRING_REDIS_HOST:$SPRING_REDIS_PORT..."

while ! nc -z $SPRING_REDIS_HOST $SPRING_REDIS_PORT; do
  sleep 2
done

echo "✅ Redis está disponível - subindo aplicação"
exec "$@"
