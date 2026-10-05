package cl.duoc.bancoxyz.transacciones.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long transaccionId;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPublicacion estado = EstadoPublicacion.PENDIENTE;

    @Column(nullable = false)
    private Integer intentos = 0;

    @Column(length = 400)
    private String ultimoError;

    @Column(nullable = false)
    private Instant creadoEn = Instant.now();
}
