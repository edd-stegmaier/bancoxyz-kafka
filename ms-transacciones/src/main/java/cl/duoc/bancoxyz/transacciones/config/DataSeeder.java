package cl.duoc.bancoxyz.transacciones.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cl.duoc.bancoxyz.transacciones.model.ClienteEntity;
import cl.duoc.bancoxyz.transacciones.model.CuentaEntity;
import cl.duoc.bancoxyz.transacciones.model.TipoCuenta;
import cl.duoc.bancoxyz.transacciones.repository.ClienteRepository;
import cl.duoc.bancoxyz.transacciones.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;

    @Override
    public void run(String... args) {
        if (clienteRepository.count() > 0) {
            return;
        }
        ClienteEntity ana = clienteRepository.save(cliente("Ana Soto", 34, "ana.soto@bancoxyz.cl", "12.345.678-9"));
        ClienteEntity luis = clienteRepository.save(cliente("Luis Perez", 41, "luis.perez@bancoxyz.cl", "9.876.543-2"));
        ClienteEntity andina = clienteRepository.save(cliente("Empresa Andina Ltda", 12, "tesoreria@andina.cl", "76.543.210-K"));

        cuentaRepository.save(cuenta("101", TipoCuenta.AHORRO, "1500000", ana));
        cuentaRepository.save(cuenta("102", TipoCuenta.AHORRO, "800000", luis));
        cuentaRepository.save(cuenta("201", TipoCuenta.HIPOTECA, "25000000", andina));
    }

    private ClienteEntity cliente(String nombre, int edad, String email, String rut) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNombre(nombre);
        cliente.setEdad(edad);
        cliente.setEmail(email);
        cliente.setRut(rut);
        return cliente;
    }

    private CuentaEntity cuenta(String numero, TipoCuenta tipo, String saldo, ClienteEntity cliente) {
        CuentaEntity cuenta = new CuentaEntity();
        cuenta.setNumeroCuenta(numero);
        cuenta.setTipoCuenta(tipo);
        cuenta.setSaldo(new BigDecimal(saldo));
        cuenta.setActiva(true);
        cuenta.setCliente(cliente);
        return cuenta;
    }
}
