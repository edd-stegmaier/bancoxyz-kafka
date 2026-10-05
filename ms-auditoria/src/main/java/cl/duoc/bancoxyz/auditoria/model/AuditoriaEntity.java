package cl.duoc.bancoxyz.auditoria.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "auditoria_transacciones")
@Data
public class AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long transaccionId;

    private String fechaEvento;
    private String numeroCuenta;
    private String cliente;
    private String tipo;

    @Column(precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(precision = 14, scale = 2)
    private BigDecimal saldoResultante;

    private String descripcion;

    @Column(nullable = false)
    private Instant consumidoEn = Instant.now();
}
