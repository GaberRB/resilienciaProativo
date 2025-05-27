package com.transacional.transacional.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transacional.transacional.model.PagamentoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(PagamentoRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            kafkaTemplate.send("topico_proativo", json);
            System.out.println("✅ Mensagem enviada ao Kafka: " + json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar o objeto para JSON", e);
        }
    }
}
