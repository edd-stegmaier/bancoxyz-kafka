package cl.duoc.bancoxyz.transacciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.bancoxyz.transacciones.model.CuentaEntity;

public interface CuentaRepository extends JpaRepository<CuentaEntity, Long> {
}
