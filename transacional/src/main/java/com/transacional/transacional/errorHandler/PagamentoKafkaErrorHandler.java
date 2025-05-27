package com.transacional.transacional.errorHandler;

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
public class PagamentoKafkaErrorHandler {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Value("${intermitente.base-url}")
    private String baseUrl;
    private static final Logger log = LoggerFactory.getLogger(PagamentoKafkaErrorHandler.class);
    public void tratarFalha(PagamentoRequest request) {
        log.info("📨 Enviando para fila...");
         kafkaProducer.sendMessage(request);
    }

}
