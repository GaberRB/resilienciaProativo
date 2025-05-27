package com.transacional.transacional.errorHandler;

import com.transacional.transacional.controller.Transacional;
import com.transacional.transacional.kafka.KafkaProducer;
import com.transacional.transacional.model.Pagamento;
import com.transacional.transacional.model.PagamentoRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class PagamentoRedisErrorHandler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${intermitente.base-url}")
    private String baseUrl;

    private static final Logger log = LoggerFactory.getLogger(PagamentoRedisErrorHandler.class);

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public void tratarFalha(PagamentoRequest request) {
        String redisKey = "pagamento:" + request.getDocumentoPagador()
                + request.getDocumentoRecebedor()
                + request.getValor();

        redisTemplate.opsForValue().set(redisKey + ":status", "PENDENTE", Duration.ofMinutes(10));
        redisTemplate.opsForValue().set(redisKey, request, Duration.ofMinutes(60));

        tentarReenviar(redisKey, request, 0);
    }

    private void tentarReenviar(String redisKey, PagamentoRequest request, int tentativa) {
        long delay = Math.min(1 + tentativa * 2, 60);

        executor.schedule(() -> {
            try {
                Pagamento pagamento = restTemplate.postForObject(baseUrl, request, Pagamento.class);

                redisTemplate.opsForValue().set(redisKey + ":status", "FEITO");
                redisTemplate.opsForValue().set(redisKey + ":response", pagamento, Duration.ofMinutes(10));

                log.info("✅ Pagamento processado após retentativa.");

            } catch (Exception e) {
                if (tentativa < 6) {
                    log.warn("🔁 Tentativa #" + tentativa + " falhou, tentando novamente em " + delay + "s");
                    tentarReenviar(redisKey, request, tentativa + 1);
                } else {
                    log.error("❌ Todas as tentativas falharam.");
                    redisTemplate.opsForValue().set(redisKey + ":status", "FALHOU");
                }
            }
        }, delay, TimeUnit.SECONDS);
    }
}
