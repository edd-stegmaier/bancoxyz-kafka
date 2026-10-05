#!/usr/bin/env bash
set -euo pipefail

AUTH="${AUTH:-http://localhost:9000}"
PRODUCER="${PRODUCER:-http://localhost:8080}"
CONSUMER="${CONSUMER:-http://localhost:8081}"

echo "1. Solicitando token OAuth2 (client_credentials)..."
TOKEN=$(curl -sf -u bancoxyz-client:secret123 \
  -d "grant_type=client_credentials&scope=transacciones.read%20transacciones.write%20auditoria.read" \
  "$AUTH/oauth2/token" | python3 -c 'import json,sys; print(json.load(sys.stdin)["access_token"])')
echo "Token obtenido (${#TOKEN} chars)"

echo "2. POST /api/transacciones sin token (debe fallar)..."
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST "$PRODUCER/api/transacciones" \
  -H "Content-Type: application/json" \
  -d '{"cuentaId":1,"tipo":"DEPOSITO","monto":1000,"descripcion":"sin token"}' || true

echo "3. Registrando retiro de alto monto..."
curl -s -X POST "$PRODUCER/api/transacciones" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cuentaId":1,"tipo":"RETIRO","monto":250000,"descripcion":"Transferencia alto monto"}'
echo

echo "4. Esperando consumo..."
sleep 3

echo "5. Auditoria:"
curl -s -H "Authorization: Bearer $TOKEN" "$CONSUMER/api/auditoria"
echo
echo "6. Alertas:"
curl -s -H "Authorization: Bearer $TOKEN" "$CONSUMER/api/alertas"
echo
