package cl.duoc.bancoxyz.auditoria.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionEvento {

    private Long id;
    private String fecha;
    private String numeroCuenta;
    private String cliente;
    private String tipo;
    private BigDecimal monto;
    private BigDecimal saldoResultante;
    private String descripcion;
}
