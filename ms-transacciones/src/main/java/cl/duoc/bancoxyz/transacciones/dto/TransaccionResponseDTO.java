package cl.duoc.bancoxyz.transacciones.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransaccionResponseDTO {

    private Long id;
    private String numeroCuenta;
    private String cliente;
    private String tipo;
    private BigDecimal monto;
    private BigDecimal saldoResultante;
    private String descripcion;
    private String publicacion;
    private String mensaje;
}
