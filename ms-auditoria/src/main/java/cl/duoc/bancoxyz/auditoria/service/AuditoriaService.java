package cl.duoc.bancoxyz.auditoria.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.auditoria.dto.TransaccionEvento;
import cl.duoc.bancoxyz.auditoria.model.AlertaEntity;
import cl.duoc.bancoxyz.auditoria.model.AuditoriaEntity;
import cl.duoc.bancoxyz.auditoria.repository.AlertaRepository;
import cl.duoc.bancoxyz.auditoria.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final AlertaRepository alertaRepository;
    private final CanalAlertaService canalAlertaService;

    @Value("${app.alerta.umbral:200000}")
    private BigDecimal umbral;

    public void registrar(TransaccionEvento evento) {
        if (auditoriaRepository.findByTransaccionId(evento.getId()).isPresent()) {
            log.info("Evento {} ya auditado, se ignora el duplicado", evento.getId());
            return;
        }
        AuditoriaEntity auditoria = new AuditoriaEntity();
        auditoria.setTransaccionId(evento.getId());
        auditoria.setFechaEvento(evento.getFecha());
        auditoria.setNumeroCuenta(evento.getNumeroCuenta());
        auditoria.setCliente(evento.getCliente());
        auditoria.setTipo(evento.getTipo());
        auditoria.setMonto(evento.getMonto());
        auditoria.setSaldoResultante(evento.getSaldoResultante());
        auditoria.setDescripcion(evento.getDescripcion());
        auditoria.setConsumidoEn(Instant.now());
        auditoriaRepository.save(auditoria);
        log.info("Auditoria persistida para transaccion {} cuenta {}", evento.getId(), evento.getNumeroCuenta());

        if (requiereAlerta(evento)) {
            AlertaEntity alerta = new AlertaEntity();
            alerta.setTransaccionId(evento.getId());
            alerta.setNumeroCuenta(evento.getNumeroCuenta());
            alerta.setCliente(evento.getCliente());
            alerta.setTipo(evento.getTipo());
            alerta.setMonto(evento.getMonto());
            alerta.setMotivo(motivo(evento));
            alerta.setCreadaEn(Instant.now());
            alerta.setEstadoEnvio(canalAlertaService.despachar(alerta));
            alertaRepository.save(alerta);
        }
    }

    public List<AuditoriaEntity> listar() {
        return auditoriaRepository.findAll();
    }

    public List<AlertaEntity> listarAlertas() {
        return alertaRepository.findAll();
    }

    private boolean requiereAlerta(TransaccionEvento evento) {
        boolean montoAlto = evento.getMonto() != null && evento.getMonto().compareTo(umbral) >= 0;
        boolean egreso = "RETIRO".equals(evento.getTipo()) || "COMPRA".equals(evento.getTipo());
        return montoAlto || (egreso && evento.getMonto() != null && evento.getMonto().compareTo(new BigDecimal("100000")) >= 0);
    }

    private String motivo(TransaccionEvento evento) {
        if (evento.getMonto() != null && evento.getMonto().compareTo(umbral) >= 0) {
            return "Monto sobre el umbral operativo de " + umbral;
        }
        return "Egreso relevante (" + evento.getTipo() + ")";
    }
}
