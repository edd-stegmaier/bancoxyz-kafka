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
@Table(name = "alertas")
@Data
public class AlertaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long transaccionId;
    private String numeroCuenta;
    private String cliente;
    private String tipo;

    @Column(precision = 14, scale = 2)
    private BigDecimal monto;

    private String motivo;

    @Column(nullable = false, length = 30)
    private String estadoEnvio;

    @Column(nullable = false)
    private Instant creadaEn = Instant.now();
}
