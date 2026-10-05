package cl.duoc.bancoxyz.auditoria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.bancoxyz.auditoria.model.AuditoriaEntity;

public interface AuditoriaRepository extends JpaRepository<AuditoriaEntity, Long> {

    Optional<AuditoriaEntity> findByTransaccionId(Long transaccionId);
}
