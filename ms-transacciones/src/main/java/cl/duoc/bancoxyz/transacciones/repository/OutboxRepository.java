package cl.duoc.bancoxyz.transacciones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.bancoxyz.transacciones.model.EstadoPublicacion;
import cl.duoc.bancoxyz.transacciones.model.OutboxEvento;

public interface OutboxRepository extends JpaRepository<OutboxEvento, Long> {

    List<OutboxEvento> findByEstado(EstadoPublicacion estado);
}
