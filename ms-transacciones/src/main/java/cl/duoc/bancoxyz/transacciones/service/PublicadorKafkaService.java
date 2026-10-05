package cl.duoc.bancoxyz.transacciones.service;

import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.transacciones.config.KafkaProducerConfig;
import cl.duoc.bancoxyz.transacciones.dto.TransaccionEvento;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicadorKafkaService {

    private final KafkaTemplate<String, TransaccionEvento> kafkaTemplate;

    @CircuitBreaker(name = "kafkaPublisher", fallbackMethod = "fallbackPublicar")
    @Retry(name = "kafkaPublisher")
    public void publicar(TransaccionEvento evento) {
        log.info("Publicando transaccion {} en {}", evento.getId(), KafkaProducerConfig.TOPIC);
        try {
            kafkaTemplate.send(KafkaProducerConfig.TOPIC, evento.getNumeroCuenta(), evento)
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo publicar en Kafka: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    private void fallbackPublicar(TransaccionEvento evento, Throwable causa) {
        log.warn("Circuit breaker kafkaPublisher abierto o agotado. Evento {} queda en outbox. Causa: {}",
                evento.getId(), causa.getMessage());
        throw new PublicacionPendienteException(causa.getMessage());
    }
}
