# BancoXYZ — Productor / Consumidor con Kafka

Entrega de microservicios basada en [bancoxyz](https://github.com/edd-stegmaier/bancoxyzBackend) implementando Kafka E3S8, OAuth2 y Resilience4j.

## Arquitectura

```
Cliente (Postman / curl)
        |  Bearer JWT (client_credentials)
        v
auth-server :9000  ---- emite JWT ----
        |                         |
        v                         v
ms-transacciones :8080      ms-auditoria :8081
(productor BancoXYZ)        (consumidor)
        |                         ^
        |  topic transacciones-bancoxyz
        |  3 particiones, RF=3, retention 12h
        v                         |
   kafka-1 / kafka-2 / kafka-3 ---+
   (leaders repartidos entre brokers)
```

| Componente | Rol | Puerto |
|---|---|---|
| `auth-server` | Authorization Server OAuth2. Emite JWT con `client_credentials`. | 9000 |
| `ms-transacciones` | Productor. CRUD reducido de BancoXYZ (clientes, cuentas, transacciones). Al registrar un movimiento publica un evento. | 8080 |
| `ms-auditoria` | Consumidor. Deja traza inmutable y genera alertas operativas (retiros, compras y montos altos). | 8081 |
| Kafka x3 + ZK x3 | Cluster. Topic `transacciones-bancoxyz`. | 29092 / 39092 / 49092 |
| Kafka UI | Inspección de particiones y mensajes. | 8090 |


## Caracteristicas

1. **OAuth2.0** — `auth-server` (Spring Authorization Server) + resource servers en ambos microservicios. Sin token válido los endpoints de negocio responden 401.
2. **Imágenes Docker** — `Dockerfile` multi-stage (Maven + Temurin 21) en cada microservicio.
3. **docker-compose** — orquesta ZK, 3 brokers, creación del topic, auth-server, productor y consumidor.
4. **Resilience4j** — circuit breaker + retry en la publicación Kafka (productor, con outbox de respaldo) y en el canal de alertas (consumidor, con fallback). Métricas en Actuator.
5. **Kafka** — 1 topic, 3 particiones, factor de replicación 3 (una réplica por broker), particionador round-robin como en el ejemplo.

## Arranque

Requisito: Docker y Docker Compose.

```bash
docker compose up --build
```

## Flujo de prueba

Token (scopes de lectura y escritura):

```bash
curl -s -u bancoxyz-client:secret123 \
  -d "grant_type=client_credentials&scope=transacciones.read transacciones.write auditoria.read" \
  http://localhost:9000/oauth2/token
```

Registrar un retiro (dispara evento y, si el monto es alto, alerta):

```bash
TOKEN=<access_token>

curl -s -X POST http://localhost:8080/api/transacciones \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cuentaId":1,"tipo":"RETIRO","monto":250000,"descripcion":"Transferencia alto monto"}'
```

Consultar auditoría y alertas:

```bash
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/auditoria
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/alertas
```

Sin token, el mismo POST debe responder 401.

Datos sembrados al partir `ms-transacciones`:

| cuentaId | numero | cliente | tipo | saldo inicial |
|---|---|---|---|---|
| 1 | 101 | Ana Soto | AHORRO | 1.500.000 |
| 2 | 102 | Luis Perez | AHORRO | 800.000 |
| 3 | 201 | Empresa Andina Ltda | HIPOTECA | 25.000.000 |

## Tolerancia a fallos

- Productor: `PublicadorKafkaService` usa `@Retry` + `@CircuitBreaker` (`kafkaPublisher`). Si Kafka no confirma, el movimiento queda en la tabla outbox como `PENDIENTE` y un scheduler lo reintenta. La transacción bancaria no se pierde.
- Consumidor: `CanalAlertaService` simula un canal inestable (falla ~40 %). Retry + circuit breaker; el fallback deja la alerta en `PENDIENTE_REINTENTO` en vez de perder el evento. El listener hace ack manual.
- Actuator: `http://localhost:8080/actuator/health` y `http://localhost:8081/actuator/circuitbreakers`.

## Pausar el consumidor

```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/kafka-listener/transaccionListener/pausar
```

## Arranque local (sin Docker de las apps)

Levantar solo el cluster y el auth-server, o correr Kafka con el compose y las apps con Maven:

```bash
# en cada módulo
mvn spring-boot:run
```

Por defecto las apps apuntan a `localhost:29092,39092,49092` y al issuer `http://localhost:9000`. Dentro de Compose esas URLs se sobreescriben con los nombres de servicio.
