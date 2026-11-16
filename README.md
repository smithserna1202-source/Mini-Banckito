# Mini-Banckito

Aplicación académica de gestión bancaria construida con Java 17, Spring Boot 3 y Maven, organizada con una arquitectura por dominio, puertos y adaptadores.

> **Uso académico únicamente.** No procesa dinero real y no debe desplegarse públicamente. La autenticación y los permisos incluidos son una base demostrativa, no una solución bancaria de producción.

## Requisitos
- JDK 17
- Maven 3.8+
- Git
- Docker Desktop con Docker Compose (recomendado para MySQL y MongoDB) o instalaciones locales de MySQL 8 y MongoDB 7+
- Visual Studio Code y la extensión **Extension Pack for Java**

## Bases de datos locales (MySQL y MongoDB)

La aplicación usa MySQL 8 para los datos relacionales y MongoDB 7 para la bitácora de auditoría. Ambos servicios se inician con Docker Compose. Desde la carpeta bank/bank:

1. Copia .env.example como .env y cambia las contraseñas de ejemplo.
2. Inicia el servidor:
   ```bash
   docker compose --env-file .env up -d
   ```
3. Exporta las variables para la aplicación (macOS/Linux):
   ```bash
   set -a
   source .env
   set +a
   ```
   En PowerShell, carga las variables equivalentes en el entorno de la terminal antes de iniciar Spring Boot.
4. Ejecuta la aplicación desde bank/bank con Maven.

También puedes usar una instalación local de MySQL 8. En ese caso configura DB_URL, DB_USERNAME y DB_PASSWORD. La cuenta de la aplicación debe tener permisos sobre la base de datos minibanckito; evita usar root en un entorno real.

Para detener MySQL sin borrar los datos:
```bash
docker compose --env-file .env down
```
Para borrar también los datos persistidos (acción irreversible):
```bash
docker compose --env-file .env down -v
```


## Abrir en Visual Studio Code

1. Clona la rama de trabajo:
   ```bash
   git clone --branch feature/completar-requisitos-pdf https://github.com/smithserna1202-source/Mini-Banckito.git
   ```
2. En VS Code selecciona **File > Open Folder** y abre `Mini-Banckito/bank/bank`.
3. Abre **Terminal > New Terminal** y ejecuta:
   ```bash
   mvn clean test
   ```
4. Configura una contraseña local de desarrollo de al menos 12 caracteres. En macOS/Linux:
   ```bash
   export BANK_DEFAULT_PASSWORD='MiClaveLocalSegura123'
   mvn spring-boot:run
   ```
   En PowerShell:
   ```powershell
   $env:BANK_DEFAULT_PASSWORD="MiClaveLocalSegura123"
   mvn spring-boot:run
   ```
5. Verifica el estado en [http://localhost:8080/api/health](http://localhost:8080/api/health).

Si mvn no se reconoce, instala Maven y configura JAVA_HOME para que apunte al JDK 17.

## Swagger / OpenAPI

Con la aplicación iniciada, abre:
- Swagger UI: http://localhost:8080/swagger-ui.html
- Especificación OpenAPI JSON: http://localhost:8080/v3/api-docs

Swagger UI y la documentación OpenAPI son públicos para facilitar la exploración; las operaciones de negocio siguen protegidas por HTTP Basic. En Swagger UI pulsa **Authorize** e ingresa el usuario y contraseña de demostración para probar endpoints protegidos.

## Usuarios y roles de demostración

La aplicación usa HTTP Basic, BCrypt y autoridades definidas en el servidor. Los usuarios de desarrollo se cargan en memoria:

| Usuario | Rol |
|---|---|
| `C001` | Cliente persona natural |
| `COMPANY001` | Cliente empresa |
| `teller01` | Empleado de ventanilla |
| `commercial01` | Empleado comercial |
| `company-operator` | Empleado operativo empresa |
| `supervisor01` | Supervisor empresa |
| `analyst01` | Analista interno |
| `admin01` | Administrador |

Todos utilizan `BANK_DEFAULT_PASSWORD`, salvo que se definan contraseñas individuales con variables `BANK_PASSWORD_NATURAL_CUSTOMER`, `BANK_PASSWORD_COMPANY_CUSTOMER`, `BANK_PASSWORD_TELLER`, `BANK_PASSWORD_COMMERCIAL`, `BANK_PASSWORD_COMPANY_OPERATOR`, `BANK_PASSWORD_COMPANY_SUPERVISOR`, `BANK_PASSWORD_ANALYST` y `BANK_PASSWORD_ADMINISTRATOR`. No reutilices contraseñas reales.

Ejemplo de consulta autenticada:
```bash
curl -u C001:$BANK_DEFAULT_PASSWORD http://localhost:8080/api/customers/C001
```

## API disponible

- `GET /api/health` — estado de la aplicación.
- `POST /api/customers`, `GET /api/customers/{id}` — registro y consulta de clientes.
- `POST /api/companies`, `GET /api/companies/{id}` — registro y consulta de empresas.
- `GET /api/products`, `POST /api/products` — catálogo de productos.
- `POST /api/accounts` — apertura de cuentas.
- `GET /api/accounts/{number}/balance` — consulta de saldo.
- `POST /api/accounts/{number}/deposit`, `POST /api/accounts/{number}/withdraw` — depósitos y retiros.
- `POST /api/transfers/internal` — transferencia interna.
- `GET /api/transfers/history/{accountNumber}` — historial de una cuenta, sujeto a permisos de titularidad o ventanilla.
- `POST /api/company-transfers`, `POST /api/company-transfers/{id}/approve`, `POST /api/company-transfers/{id}/reject` — transferencias de empresa.
- `POST /api/scheduled-transfers`, `POST /api/scheduled-transfers/{id}/cancel` — transferencias programadas.
- `POST /api/loans`, `POST /api/loans/{id}/approve`, `POST /api/loans/{id}/reject`, `POST /api/loans/{id}/disburse` — solicitudes, decisiones y desembolsos de préstamos.
- `GET /api/audit` — bitácora completa, solo para analistas internos y administradores.
- `GET /api/audit/product/{productId}` — historial de auditoría de una cuenta o préstamo propio para clientes.

Ejemplo de apertura de cuenta:
```bash
curl -u teller01:$BANK_DEFAULT_PASSWORD -X POST http://localhost:8080/api/accounts \
 -H "Content-Type: application/json" \
 -d '{"accountNumber":"1234567890","ownerId":"C001","type":"SAVINGS","initialBalance":100000,"currency":"COP"}'
```

## Persistencia y límites conocidos

- Los perfiles predeterminados `sql,mongodb` activan MySQL 8 para clientes, empresas, cuentas, productos, préstamos, transferencias y movimientos; MongoDB 7 guarda la bitácora de auditoría mediante inserciones.
- Hibernate crea/actualiza el esquema relacional automáticamente (`ddl-auto=update`); respalda la base de datos antes de actualizar el proyecto.
- Las transferencias empresariales que superan `BANK_COMPANY_TRANSFER_APPROVAL_THRESHOLD` requieren aprobación, y las transferencias pendientes vencen después de 60 minutos. Las transferencias ejecutadas registran débitos y créditos en el historial SQL. Las transferencias programadas se procesan cada 30 segundos.
- El servicio interbancario externo rechaza la operación porque no existe integración de liquidación con otra entidad.
- El umbral de aprobación de transferencias empresariales es configurable mediante `BANK_COMPANY_TRANSFER_APPROVAL_THRESHOLD` (valor inicial de demostración: 5.000.000). El enunciado académico no fija un importe universal ni define un límite diario; por eso no se inventa un tope diario.
- Los usuarios/roles de demostración se precargan en memoria; falta gestión completa de credenciales, altas/bajas y asociación persistente entre empleados, clientes y empresas.
- La configuración de seguridad es demostrativa para uso académico, no apta para producción bancaria.

## Pruebas

Desde bank/bank:
```bash
mvn clean test
```
Las pruebas automatizadas usan H2 en memoria y no requieren levantar MySQL. El flujo de integración continua también inicia MySQL 8 y comprueba el arranque real de la API contra esa base de datos, además de verificar Swagger.
Desde la raíz:
```bash
mvn -f bank/bank/pom.xml clean test
```

La rama `feature/completar-requisitos-pdf` contiene los cambios de desarrollo. No se integra a `main` hasta que la compilación y la revisión funcional estén correctas.
