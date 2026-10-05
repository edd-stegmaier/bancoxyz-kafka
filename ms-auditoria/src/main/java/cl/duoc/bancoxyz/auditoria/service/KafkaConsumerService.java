package cl.duoc.bancoxyz.auditoria.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.auditoria.config.KafkaConsumerConfig;
import cl.duoc.bancoxyz.auditoria.dto.TransaccionEvento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final AuditoriaService auditoriaService;

    @KafkaListener(id = "transaccionListener", topics = KafkaConsumerConfig.TOPIC,
            groupId = KafkaConsumerConfig.CONSUMER_GROUP_ID)
    public void consumir(TransaccionEvento evento, Acknowledgment ack) {
        try {
            log.info("Mensaje consumido: {}", evento);
            auditoriaService.registrar(evento);
            ack.acknowledge();
            log.info("Acknowledge realizado para transaccion {}", evento.getId());
        } catch (Exception e) {
            log.error("Error consumiendo transaccion {}", evento == null ? null : evento.getId(), e);
        }
    }
}
