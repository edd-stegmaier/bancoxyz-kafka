package cl.duoc.bancoxyz.transacciones.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CuentaResponseDTO {

    private Long id;
    private String numeroCuenta;
    private String tipoCuenta;
    private BigDecimal saldo;
    private Boolean activa;
    private String cliente;
}
