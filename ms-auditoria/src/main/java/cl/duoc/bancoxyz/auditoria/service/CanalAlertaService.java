package cl.duoc.bancoxyz.auditoria.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.auditoria.model.AlertaEntity;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CanalAlertaService {

    @CircuitBreaker(name = "canalAlerta", fallbackMethod = "fallback")
    @Retry(name = "canalAlerta")
    public String despachar(AlertaEntity alerta) {
        int escenario = ThreadLocalRandom.current().nextInt(100);
        if (escenario < 40) {
            log.warn("Canal de alertas inestable para transaccion {}", alerta.getTransaccionId());
            throw new RuntimeException("Canal de alertas no disponible");
        }
        log.info("Alerta enviada por canal operativo. transaccion={}", alerta.getTransaccionId());
        return "ENVIADA";
    }

    @SuppressWarnings("unused")
    private String fallback(AlertaEntity alerta, Throwable causa) {
        log.warn("Fallback canalAlerta. Alerta {} queda pendiente. Causa: {}",
                alerta.getTransaccionId(), causa.getMessage());
        return "PENDIENTE_REINTENTO";
    }
}
