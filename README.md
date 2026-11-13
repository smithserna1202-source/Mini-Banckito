# Mini-Banckito

Aplicación académica de gestión bancaria construida con Java 17, Spring Boot 3 y Maven, organizada con una arquitectura por dominio, puertos y adaptadores.

> **Uso académico únicamente.** No procesa dinero real y no debe desplegarse públicamente. La autenticación y los permisos incluidos son una base demostrativa, no una solución bancaria de producción.

## Requisitos
- JDK 17
- Maven 3.8+
- Git
- Visual Studio Code y la extensión **Extension Pack for Java**

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

Si `mvn` no se reconoce, instala Maven y configura `JAVA_HOME` para que apunte al JDK 17.

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
- `GET /api/audit` — consulta restringida de bitácora.

Ejemplo de apertura de cuenta:
```bash
curl -u teller01:$BANK_DEFAULT_PASSWORD -X POST http://localhost:8080/api/accounts \
 -H "Content-Type: application/json" \
 -d '{"accountNumber":"1234567890","ownerId":"C001","type":"SAVINGS","initialBalance":100000,"currency":"COP"}'
```

## Persistencia y límites conocidos

- El perfil predeterminado es `sql`: H2 guarda cuentas, clientes, empresas y productos en un archivo local bajo `bank/bank/data`.
- Préstamos, transferencias internas/empresariales, transferencias programadas y bitácora todavía usan adaptadores en memoria; esos datos se pierden al reiniciar. Las transacciones de depósitos, retiros y transferencias internas se guardan en SQL.
- La bitácora es de solo anexado desde los servicios de dominio; la persistencia MongoDB requiere configurar y probar el perfil y el servidor Mongo.
- El vencimiento de transferencias empresariales se revisa automáticamente cada minuto; las transferencias programadas se procesan cada 30 segundos.
- El servicio interbancario externo rechaza la operación porque no existe integración de liquidación con otra entidad.
- El validador de límites existe en dominio, pero el acumulado diario aún no está conectado a un historial persistente.
- Los usuarios/roles de demostración son precargados; falta gestión completa de credenciales, altas/bajas y asociación persistente entre empleados, clientes y empresas.

## Pruebas

Desde `bank/bank`:
```bash
mvn clean test
```
Desde la raíz:
```bash
mvn -f bank/bank/pom.xml clean test
```

La rama `feature/completar-requisitos-pdf` contiene los cambios de desarrollo. No se integra a `main` hasta que la compilación y la revisión funcional estén correctas.
