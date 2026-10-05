package cl.duoc.bancoxyz.transacciones.dto;

import java.math.BigDecimal;

import cl.duoc.bancoxyz.transacciones.model.TipoTransaccion;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransaccionRequestDTO {

    @NotNull
    private Long cuentaId;

    @NotNull
    private TipoTransaccion tipo;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal monto;

    private String descripcion;
}
