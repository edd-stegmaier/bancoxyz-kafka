package cl.duoc.bancoxyz.auditoria.service;

import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaAdminListenerService {

    private final KafkaListenerEndpointRegistry registry;

    public void pausar(String id) {
        registry.getListenerContainer(id).pause();
    }

    public void reanudar(String id) {
        registry.getListenerContainer(id).resume();
    }

    public boolean pausado(String id) {
        return registry.getListenerContainer(id).isPauseRequested();
    }
}
