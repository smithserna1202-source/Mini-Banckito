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

La aplicación usa HTTP Basic, BCrypt y autoridades definidas en el servidor. Los usuarios iniciales de demostración se crean una sola vez en MySQL; sus contraseñas se almacenan con BCrypt. Los cambios de contraseña y las desactivaciones persisten entre reinicios:

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

Para asociar los usuarios empresariales a una compañía concreta, configura también
`BANK_COMPANY_ID_COMPANY_CUSTOMER`, `BANK_COMPANY_ID_COMPANY_OPERATOR` y
`BANK_COMPANY_ID_COMPANY_SUPERVISOR`. En el entorno académico los tres valores
inician como `COMPANY001`; cámbialos por el identificador real de cada empresa
de prueba si trabajas con más de una compañía.

Ejemplo de consulta autenticada:
```bash
curl -u C001:$BANK_DEFAULT_PASSWORD http://localhost:8080/api/customers/C001
```

## Administración de usuarios

Al iniciar por primera vez, la aplicación crea usuarios de demostración en la tabla `bank_users` usando `BANK_DEFAULT_PASSWORD` o las contraseñas individuales configuradas. No sobrescribe usuarios existentes.

El rol `ADMINISTRATOR` puede gestionar todos los usuarios. Los roles `COMPANY_CUSTOMER` y `COMPANY_SUPERVISOR` pueden crear y desactivar únicamente usuarios operativos de su propia empresa. Las altas requieren nombre, identificación, correo, teléfono, fecha de nacimiento, dirección, rol y entidad relacionada. Las operaciones disponibles son:
- `POST /api/admin/users` — crear un usuario con nombre, contraseña (mínimo 12 caracteres), rol y entidad asociada.
- `POST /api/admin/users/{username}/deactivate` y `POST /api/admin/users/{username}/activate` — desactivar o reactivar usuarios; para clientes naturales y empresas, el estado del perfil se sincroniza con el estado de acceso.
- `POST /api/admin/users/{username}/customers/{customerId}` — asignar un cliente activo a un empleado comercial.

El empleado comercial solo puede consultar los perfiles que tenga asignados.

## API disponible

- `GET /api/health` — estado de la aplicación.
- `POST /api/customers`, `GET /api/customers/{id}`, `POST /api/customers/{id}/block`, `POST /api/customers/{id}/unblock`, `POST /api/customers/{id}/deactivate`, `POST /api/customers/{id}/activate` — registro, consulta y control de estado de clientes.
- `POST /api/companies`, `GET /api/companies/{id}`, `POST /api/companies/{id}/block`, `POST /api/companies/{id}/unblock`, `POST /api/companies/{id}/deactivate`, `POST /api/companies/{id}/activate` — registro, consulta y control de estado de empresas.
- `GET /api/products`, `POST /api/products` — catálogo de productos.
- `POST /api/accounts` — apertura de cuentas.
- `GET /api/accounts` — listado filtrado por titular, empresa o clientes asignados.
- `GET /api/accounts/{number}/balance` — consulta de saldo.
- `POST /api/accounts/{number}/deposit`, `POST /api/accounts/{number}/withdraw` — depósitos y retiros.
- `POST /api/transfers/internal` — transferencia interna.
- `GET /api/transfers/history/{accountNumber}` — historial de una cuenta, sujeto a permisos de titularidad o ventanilla.
- `POST /api/company-transfers`, `POST /api/company-transfers/batch`, `GET /api/company-transfers/pending`, `POST /api/company-transfers/{id}/approve`, `POST /api/company-transfers/{id}/reject` — transferencias de empresa y cola de aprobación filtrada por compañía.
- `POST /api/scheduled-transfers`, `POST /api/scheduled-transfers/{id}/cancel` — transferencias programadas.
- `POST /api/loans`, `GET /api/loans`, `GET /api/loans/{id}`, `POST /api/loans/{id}/approve`, `POST /api/loans/{id}/reject`, `POST /api/loans/{id}/disburse` — solicitudes, consulta según rol, decisiones y desembolsos de préstamos.
- `GET /api/audit` — bitácora completa, solo para analistas internos y administradores.
- `GET /api/audit/product/{productId}` — historial de auditoría de una cuenta, préstamo o transferencia que involucre productos propios o de la empresa.

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
- Los usuarios y sus datos básicos se guardan en MySQL; las credenciales usan BCrypt y las cuentas desactivadas no pueden autenticarse. Los administradores pueden crear/desactivar usuarios y asignar clientes a empleados comerciales; los administradores de empresa gestionan sus usuarios operativos dentro de su propia compañía. Los empleados comerciales pueden solicitar cuentas sin saldo inicial y préstamos solo para clientes asignados. La asociación de usuarios empresariales se configura mediante `BANK_COMPANY_ID_*`.
- El catálogo se inicializa con los cuatro tipos de cuenta del enunciado y productos de préstamo/servicio. La apertura de cuentas valida que el tipo solicitado siga activo en el catálogo.
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
