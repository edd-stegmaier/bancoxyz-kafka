package cl.duoc.bancoxyz.transacciones.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bancoxyz.transacciones.dto.CuentaResponseDTO;
import cl.duoc.bancoxyz.transacciones.dto.TransaccionRequestDTO;
import cl.duoc.bancoxyz.transacciones.dto.TransaccionResponseDTO;
import cl.duoc.bancoxyz.transacciones.model.OutboxEvento;
import cl.duoc.bancoxyz.transacciones.service.TransaccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;

    @GetMapping("/cuentas")
    public List<CuentaResponseDTO> cuentas() {
        return transaccionService.listarCuentas();
    }

    @GetMapping("/transacciones")
    public List<TransaccionResponseDTO> transacciones() {
        return transaccionService.listarTransacciones();
    }

    @PostMapping("/transacciones")
    public ResponseEntity<TransaccionResponseDTO> crear(@Valid @RequestBody TransaccionRequestDTO request) {
        TransaccionResponseDTO response = transaccionService.registrar(request);
        return ResponseEntity.status("ENVIADO".equals(response.getPublicacion()) ? 201 : 202).body(response);
    }

    @GetMapping("/outbox")
    public List<OutboxEvento> outbox() {
        return transaccionService.listarOutbox();
    }
}
