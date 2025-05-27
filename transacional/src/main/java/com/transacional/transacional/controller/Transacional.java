package com.transacional.transacional.controller;

import com.transacional.transacional.errorHandler.PagamentoKafkaErrorHandler;
import com.transacional.transacional.errorHandler.PagamentoRedisErrorHandler;
import com.transacional.transacional.model.Pagamento;
import com.transacional.transacional.model.PagamentoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@RestController
@RequestMapping("/transacional")
public class Transacional {


    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PagamentoRedisErrorHandler redisErrorHandler;
    private final PagamentoKafkaErrorHandler kafkaErrorHandler;
    private final String baseUrl;

    private static final Logger log = LoggerFactory.getLogger(Transacional.class);

    @Autowired
    public Transacional(RestTemplate restTemplate,
                                  RedisTemplate<String, Object> redisTemplate,
                                  PagamentoRedisErrorHandler redisErrorHandler,
                                  PagamentoKafkaErrorHandler kafkaErrorHandler,
                                  @Value("${intermitente.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.redisErrorHandler = redisErrorHandler;
        this.kafkaErrorHandler = kafkaErrorHandler;
        this.baseUrl = baseUrl;
    }

    @PostMapping("/operacao0")
    public ResponseEntity<?> chamarIntermitente(@RequestBody PagamentoRequest request) {
        try {
            Pagamento pagamento = restTemplate.postForObject(baseUrl, request, Pagamento.class);
            return ResponseEntity.ok(pagamento);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Erro ao processar o pagamento");
        }
    }

    @PostMapping("/operacaoRedis")
    public ResponseEntity<?> chamarIntermitenteRedis(@RequestBody PagamentoRequest request) {
        String redisKey = "pagamento:" + request.getDocumentoPagador() + request.getDocumentoRecebedor() + request.getValor();
        String statusKey = redisKey + ":status";
        String responseKey = redisKey + ":response";

        String status = (String) redisTemplate.opsForValue().get(statusKey);

        if ("PENDENTE".equals(status)) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Já estamos processando sua solicitação. Por favor aguarde.");
        }

        if ("FEITO".equals(status)) {
            Pagamento pagamento = (Pagamento) redisTemplate.opsForValue().get(responseKey);

            if (pagamento == null){
               log.error("⛔ pagamento nulo");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR");
            }
            log.info("✅ Status Feito pagamento: .", pagamento);
            return ResponseEntity.ok(pagamento);
        }

        try {
            Pagamento pagamento = restTemplate.postForObject(baseUrl, request, Pagamento.class);
            return ResponseEntity.ok(pagamento);
        } catch (Exception e) {
            redisTemplate.opsForValue().set(statusKey, "PENDENTE", Duration.ofMinutes(10));
            redisErrorHandler.tratarFalha(request);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Não conseguimos processar agora. Estamos cuidando disso para você.");
        }
    }

    @PostMapping("/operacaoKafka")
    public ResponseEntity<?> chamarIntermitenteKafka(@RequestBody PagamentoRequest request) {
        String redisKey = "pagamento:" + request.getDocumentoPagador() + request.getDocumentoRecebedor() + request.getValor();
        String statusKey = redisKey + ":status";
        String responseKey = redisKey + ":response";

        String status = (String) redisTemplate.opsForValue().get(statusKey);

        if ("PENDENTE".equals(status)) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Já estamos processando sua solicitação. Por favor aguarde.");
        }

        if ("FEITO".equals(status)) {
            Pagamento pagamento = (Pagamento) redisTemplate.opsForValue().get(responseKey);

            if (pagamento == null){
                log.error("⛔ pagamento nulo");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR");
            }
            log.info("✅ Status Feito pagamento: .", pagamento);
            return ResponseEntity.ok(pagamento);
        }

        try {
            Pagamento pagamento = restTemplate.postForObject(baseUrl, request, Pagamento.class);
            return ResponseEntity.ok(pagamento);
        } catch (Exception e) {
            redisTemplate.opsForValue().set(statusKey, "PENDENTE", Duration.ofMinutes(10));
            kafkaErrorHandler.tratarFalha(request);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Não conseguimos processar agora. Estamos cuidando disso para você.");
        }
    }

}


