# Guion de demostración en Swagger

Duración sugerida: 3 a 5 minutos. La demostración debe hacerse en un entorno local con datos ficticios.

## 1. Preparación

Desde `bank/bank`:

```bash
cp .env.example .env
# Editar .env y establecer contraseñas locales propias
docker compose --env-file .env up -d
set -a
source .env
set +a
export BANK_DEFAULT_PASSWORD='ClaveLocalDemo123!'
mvn spring-boot:run
```

Abrir:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health: http://localhost:8080/api/health

En Swagger, pulsar **Authorize** e introducir el usuario de demostración y la contraseña local configurada. No grabar contraseñas reales ni datos personales.

## 2. Recorrido recomendado

1. Mostrar `GET /api/health` y confirmar que la API responde.
2. Mostrar `GET /api/products` con usuario `C001` y el catálogo disponible.
3. Autorizar como `teller01` y mostrar `GET /api/accounts`.
4. Crear una cuenta de demostración con `POST /api/accounts`. Usar un número de cuenta nuevo de 10 dígitos y titular `C001`. Ejemplo:

   ```json
   {
     "accountNumber": "1234567891",
     "ownerId": "C001",
     "type": "SAVINGS",
     "initialBalance": 100000,
     "currency": "COP"
   }
   ```

5. Hacer un depósito con `POST /api/accounts/{number}/deposit`:

   ```json
   { "amount": 25000 }
   ```

6. Consultar el saldo con `GET /api/accounts/{number}/balance` y el historial con `GET /api/transfers/history/{accountNumber}`.
7. Mostrar un intento de acceso no autorizado para evidenciar que los permisos por rol se aplican.
8. Autorizar como `analyst01` y mostrar `GET /api/audit`, donde quedan registrados los eventos.
9. Mostrar la documentación OpenAPI y la estructura de endpoints.

## 3. Roles de demostración

Los usuarios iniciales son `C001`, `COMPANY001`, `teller01`, `commercial01`, `company-operator`, `supervisor01`, `analyst01` y `admin01`. Todos utilizan la contraseña local configurada en `BANK_DEFAULT_PASSWORD`, salvo que se hayan definido contraseñas individuales.

## 4. Cierre de la demostración

Explicar que MySQL conserva los datos relacionales y MongoDB almacena la bitácora de auditoría. El proyecto es académico: no procesa dinero real y no debe exponerse públicamente.

## 5. Apagar servicios

Detener Spring Boot con `Ctrl+C`. Para detener las bases de datos sin borrar su información:

```bash
docker compose --env-file .env down
```
