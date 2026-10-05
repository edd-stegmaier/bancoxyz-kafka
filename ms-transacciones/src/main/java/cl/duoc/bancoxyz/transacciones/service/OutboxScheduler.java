package cl.duoc.bancoxyz.transacciones.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final TransaccionService transaccionService;

    @Scheduled(fixedDelay = 15000)
    public void republicarPendientes() {
        log.debug("Reintentando eventos pendientes del outbox");
        transaccionService.reintentarPendientes();
    }
}
