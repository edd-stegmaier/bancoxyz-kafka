package cl.duoc.bancoxyz.transacciones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.bancoxyz.transacciones.model.TransaccionEntity;

public interface TransaccionRepository extends JpaRepository<TransaccionEntity, Long> {

    List<TransaccionEntity> findByCuentaIdOrderByIdDesc(Long cuentaId);
}
