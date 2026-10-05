package cl.duoc.bancoxyz.auditoria.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bancoxyz.auditoria.model.AlertaEntity;
import cl.duoc.bancoxyz.auditoria.model.AuditoriaEntity;
import cl.duoc.bancoxyz.auditoria.service.AuditoriaService;
import cl.duoc.bancoxyz.auditoria.service.KafkaAdminListenerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;
    private final KafkaAdminListenerService adminListenerService;

    @GetMapping("/auditoria")
    public List<AuditoriaEntity> auditoria() {
        return auditoriaService.listar();
    }

    @GetMapping("/alertas")
    public List<AlertaEntity> alertas() {
        return auditoriaService.listarAlertas();
    }

    @PostMapping("/kafka-listener/{id}/pausar")
    public String pausar(@PathVariable String id) {
        adminListenerService.pausar(id);
        return "Listener " + id + " pausado";
    }

    @PostMapping("/kafka-listener/{id}/reanudar")
    public String reanudar(@PathVariable String id) {
        adminListenerService.reanudar(id);
        return "Listener " + id + " reanudado";
    }

    @GetMapping("/kafka-listener/{id}/estado")
    public String estado(@PathVariable String id) {
        return "Listener " + id + " esta " + (adminListenerService.pausado(id) ? "detenido" : "activo");
    }
}
