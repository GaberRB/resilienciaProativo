package com.transacional.transacional.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class StartupRedisHealthCheck {

    private static final Logger log = LoggerFactory.getLogger(StartupRedisHealthCheck.class);
    private final RedisConnectionFactory redisConnectionFactory;

    public StartupRedisHealthCheck(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @PostConstruct
    public void checkRedisConnection() {
        int tentativas = 5;

        while (tentativas > 0) {
            try (var connection = redisConnectionFactory.getConnection()) {
                String ping = connection.ping();
                if ("PONG".equals(ping)) {
                    log.info("✅ Redis está conectado e respondeu com PONG.");
                    return;
                } else {
                    throw new RuntimeException("⚠️ Redis respondeu, mas não foi PONG. Resposta: " + ping);
                }
            } catch (Exception e) {
                tentativas--;
                log.warn("❌ Falha na conexão com Redis. Tentativas restantes: {}. Erro: {}", tentativas, e.getMessage());
                if (tentativas == 0) {
                    log.error("⛔ Não foi possível conectar no Redis após múltiplas tentativas. Aplicação será encerrada.");
                    throw new RuntimeException("❌ Erro crítico: Falha ao conectar no Redis na inicialização.", e);
                }
                try {
                    Thread.sleep(2000); // Espera 2 segundos antes da próxima tentativa
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrompida durante retry de conexão Redis.", ie);
                }
            }
        }
    }
}