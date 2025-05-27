package com.intermitente.intermitente.controller;

import com.intermitente.intermitente.model.Pagamento;
import com.intermitente.intermitente.model.PagamentoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
@RestController
@RequestMapping("/pagamentos")
public class Pagamentos {
    private static final long START_TIME = System.currentTimeMillis();
    private static final long SIMULATION_DURATION = 10 * 60 * 1000; // 10 minutos
    private static long intermitenteInicio = -1;
    private static long intermitenteDuracao = -1;

    @PostMapping("/pagar")
    public ResponseEntity<Pagamento> pagar(@RequestBody PagamentoRequest request) {
        long tempoDecorrido = System.currentTimeMillis() - START_TIME;

        // Se a simulação passou de 10 minutos, reiniciamos o tempo
        if (tempoDecorrido > SIMULATION_DURATION) {
            intermitenteInicio = -1;
            intermitenteDuracao = -1;
        }

        // Criar períodos intermitentes aleatórios
        if (intermitenteInicio == -1 || tempoDecorrido > intermitenteInicio + intermitenteDuracao) {
            if (ThreadLocalRandom.current().nextDouble() < 0.25) { // 25% de chance de um período intermitente ocorrer
                intermitenteInicio = tempoDecorrido;
                intermitenteDuracao = ThreadLocalRandom.current().nextInt(30_000, 180_000); // Entre 30s e 3min
            } else {
                intermitenteInicio = -1;
                intermitenteDuracao = -1;
            }
        }

        // Se estamos dentro do período intermitente, simulamos alta latência e erro
        if (intermitenteInicio != -1 && tempoDecorrido >= intermitenteInicio && tempoDecorrido < intermitenteInicio + intermitenteDuracao) {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(2000, 5000)); // Simula lentidão entre 2s e 5s
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Pagamento pagamentoErro = new Pagamento(UUID.randomUUID(), request.getValor(), request.getDocumentoPagador(), request.getDocumentoRecebedor(), "INTERMITENTE", LocalDateTime.now());
            return new ResponseEntity<>(pagamentoErro, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        UUID id = UUID.randomUUID();
        Random random = new Random();
        int chance = random.nextInt(100);

        String status;
        if (chance < 1) {
            status = "ERRO";
        } else if (chance < 21) {
            status = "AGENDADO";
        } else {
            status = "PAGO";
        }

        Pagamento pagamento = new Pagamento(id,request.getValor(), request.getDocumentoPagador(), request.getDocumentoRecebedor(), status, LocalDateTime.now());

        return new ResponseEntity<>(pagamento, HttpStatus.OK);
    }
}

