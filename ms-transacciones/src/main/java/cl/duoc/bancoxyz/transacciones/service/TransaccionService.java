package cl.duoc.bancoxyz.transacciones.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.bancoxyz.transacciones.dto.CuentaResponseDTO;
import cl.duoc.bancoxyz.transacciones.dto.TransaccionEvento;
import cl.duoc.bancoxyz.transacciones.dto.TransaccionRequestDTO;
import cl.duoc.bancoxyz.transacciones.dto.TransaccionResponseDTO;
import cl.duoc.bancoxyz.transacciones.model.CuentaEntity;
import cl.duoc.bancoxyz.transacciones.model.EstadoPublicacion;
import cl.duoc.bancoxyz.transacciones.model.OutboxEvento;
import cl.duoc.bancoxyz.transacciones.model.TipoTransaccion;
import cl.duoc.bancoxyz.transacciones.model.TransaccionEntity;
import cl.duoc.bancoxyz.transacciones.repository.CuentaRepository;
import cl.duoc.bancoxyz.transacciones.repository.OutboxRepository;
import cl.duoc.bancoxyz.transacciones.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final OutboxRepository outboxRepository;
    private final PublicadorKafkaService publicador;
    private final ObjectMapper objectMapper;

    @Transactional
    public TransaccionResponseDTO registrar(TransaccionRequestDTO request) {
        CuentaEntity cuenta = cuentaRepository.findById(request.getCuentaId())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + request.getCuentaId()));
        if (!Boolean.TRUE.equals(cuenta.getActiva())) {
            throw new IllegalArgumentException("La cuenta " + cuenta.getNumeroCuenta() + " no esta activa");
        }

        BigDecimal saldo = aplicarMovimiento(cuenta, request.getTipo(), request.getMonto());
        cuenta.setSaldo(saldo);
        cuentaRepository.save(cuenta);

        TransaccionEntity transaccion = new TransaccionEntity();
        transaccion.setFecha(LocalDate.now());
        transaccion.setTipo(request.getTipo());
        transaccion.setMonto(request.getMonto());
        transaccion.setDescripcion(request.getDescripcion());
        transaccion.setCuenta(cuenta);
        transaccion = transaccionRepository.save(transaccion);

        TransaccionEvento evento = new TransaccionEvento(
                transaccion.getId(),
                Instant.now().toString(),
                cuenta.getNumeroCuenta(),
                cuenta.getCliente().getNombre(),
                request.getTipo().name(),
                request.getMonto(),
                saldo,
                request.getDescripcion());

        OutboxEvento outbox = new OutboxEvento();
        outbox.setTransaccionId(transaccion.getId());
        outbox.setPayload(toJson(evento));
        outbox.setEstado(EstadoPublicacion.PENDIENTE);
        outbox.setIntentos(0);
        outbox.setCreadoEn(Instant.now());
        outbox = outboxRepository.save(outbox);

        String publicacion = intentarPublicar(outbox, evento);
        return new TransaccionResponseDTO(
                transaccion.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getCliente().getNombre(),
                request.getTipo().name(),
                request.getMonto(),
                saldo,
                request.getDescripcion(),
                publicacion,
                "ENVIADO".equals(publicacion)
                        ? "Movimiento registrado y publicado en Kafka"
                        : "Movimiento registrado. Publicacion pendiente por tolerancia a fallos");
    }

    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> listarCuentas() {
        return cuentaRepository.findAll().stream()
                .map(c -> new CuentaResponseDTO(
                        c.getId(),
                        c.getNumeroCuenta(),
                        c.getTipoCuenta().name(),
                        c.getSaldo(),
                        c.getActiva(),
                        c.getCliente().getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransaccionResponseDTO> listarTransacciones() {
        return transaccionRepository.findAll().stream()
                .map(t -> new TransaccionResponseDTO(
                        t.getId(),
                        t.getCuenta().getNumeroCuenta(),
                        t.getCuenta().getCliente().getNombre(),
                        t.getTipo().name(),
                        t.getMonto(),
                        t.getCuenta().getSaldo(),
                        t.getDescripcion(),
                        "CONSULTA",
                        "Movimiento registrado"))
                .toList();
    }

    public List<OutboxEvento> listarOutbox() {
        return outboxRepository.findAll();
    }

    public void reintentarPendientes() {
        for (OutboxEvento outbox : outboxRepository.findByEstado(EstadoPublicacion.PENDIENTE)) {
            try {
                TransaccionEvento evento = objectMapper.readValue(outbox.getPayload(), TransaccionEvento.class);
                intentarPublicar(outbox, evento);
            } catch (Exception e) {
                log.warn("No se pudo reintentar outbox {}: {}", outbox.getId(), e.getMessage());
            }
        }
    }

    private String intentarPublicar(OutboxEvento outbox, TransaccionEvento evento) {
        try {
            publicador.publicar(evento);
            outbox.setEstado(EstadoPublicacion.ENVIADO);
            outbox.setIntentos(outbox.getIntentos() + 1);
            outbox.setUltimoError(null);
            outboxRepository.save(outbox);
            return "ENVIADO";
        } catch (PublicacionPendienteException e) {
            outbox.setEstado(EstadoPublicacion.PENDIENTE);
            outbox.setIntentos(outbox.getIntentos() + 1);
            outbox.setUltimoError(e.getMessage());
            outboxRepository.save(outbox);
            return "PENDIENTE";
        }
    }

    private BigDecimal aplicarMovimiento(CuentaEntity cuenta, TipoTransaccion tipo, BigDecimal monto) {
        if (tipo == TipoTransaccion.DEPOSITO) {
            return cuenta.getSaldo().add(monto);
        }
        if (cuenta.getSaldo().compareTo(monto) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente en la cuenta " + cuenta.getNumeroCuenta());
        }
        return cuenta.getSaldo().subtract(monto);
    }

    private String toJson(TransaccionEvento evento) {
        try {
            return objectMapper.writeValueAsString(evento);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el evento", e);
        }
    }
}
