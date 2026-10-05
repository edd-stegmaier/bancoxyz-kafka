package cl.duoc.bancoxyz.transacciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.bancoxyz.transacciones.model.ClienteEntity;

public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {
}
