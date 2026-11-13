# Mini-Banckito

Aplicación académica de gestión bancaria con Java 17, Spring Boot 3 y Maven. El código se organiza en dominio, puertos, adaptadores e interfaz HTTP.

> **Importante:** es un prototipo educativo. No debe manejar dinero real ni exponerse a Internet. La autorización por roles del dominio no sustituye autenticación segura de usuarios ni controles de acceso en cada endpoint.

## Requisitos
- JDK 17
- Maven 3.8 o superior
- Git
- Visual Studio Code y extensión **Extension Pack for Java**

## Abrir en Visual Studio Code

1. Clona la rama de trabajo:
   ```bash
   git clone --branch feature/completar-requisitos-pdf https://github.com/smithserna1202-source/Mini-Banckito.git
   ```
2. En VS Code selecciona **File > Open Folder** y abre `Mini-Banckito/bank/bank`.
3. Espera a que la extensión de Java importe el proyecto Maven.
4. Abre una terminal integrada (**Terminal > New Terminal**) y ejecuta:
   ```bash
   mvn clean test
   mvn spring-boot:run
   ```
5. Comprueba que la API responde:
   ```bash
   curl http://localhost:8080/api/health
   ```

Si el comando `mvn` no se reconoce, instala Maven y configura `JAVA_HOME` para que apunte al JDK 17. También puedes usar la terminal de VS Code.

## Endpoints disponibles en esta versión

- `GET /api/health` — estado de la aplicación.
- `POST /api/customers` y `GET /api/customers/{id}` — registro y consulta de clientes.
- `POST /api/accounts` — apertura de cuenta.
- `GET /api/accounts/{number}/balance` — consulta de saldo.
- `POST /api/accounts/{number}/deposit` — depósito.
- `POST /api/accounts/{number}/withdraw` — retiro.
- `POST /api/transfers/internal` — transferencia interna.
- `POST /api/loans`, `POST /api/loans/{id}/approve`, `POST /api/loans/{id}/reject` — ciclo inicial de préstamo.
- `POST /api/company-transfers`, `POST /api/company-transfers/{id}/approve`, `POST /api/company-transfers/{id}/reject` — transferencias de empresa.
- `POST /api/scheduled-transfers` y `POST /api/scheduled-transfers/{id}/cancel` — programación y cancelación.

### Ejemplo: abrir una cuenta

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"accountNumber":"1234567890","ownerId":"C001","type":"SAVINGS","initialBalance":100000,"currency":"COP"}'
```

### Ejemplo: consultar saldo

```bash
curl http://localhost:8080/api/accounts/1234567890/balance
```

### Ejemplo: registrar un cliente

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"id":"C001","name":"Cliente de prueba","identification":"1000000001","email":"cliente@example.com","phone":"3001234567","birthDate":"2000-01-01","address":"Medellin"}'
```

## Persistencia y limitaciones pendientes

- La configuración local actual usa repositorios en memoria para los flujos principales; los datos se pierden al reiniciar.
- Se añadió una primera base de adaptadores JPA/H2 para cuentas y clientes, pero falta completar el mapeo y la persistencia SQL de préstamos, transferencias, usuarios y productos.
- Existe el modelo de auditoría y un adaptador MongoDB de inserción, pero la bitácora necesita una configuración operativa, pruebas de integración y endurecimiento de inmutabilidad.
- El vencimiento de transferencias pendientes tiene una operación de revisión; falta programar su ejecución automática periódica.
- La integración interbancaria externa no está implementada: el servicio rechaza esa operación de forma explícita para no debitar fondos sin liquidación real.
- La autenticación HTTP, sesiones/tokens y autorización real por endpoint siguen pendientes. Los campos de rol enviados en los cuerpos de las peticiones son datos de demostración y no son una identidad autenticada.
- Faltan completar los requisitos de productos, empresas, asignación de clientes, límites diarios persistentes, pagos y pruebas de integración de todos los flujos.

## Ejecutar pruebas

Desde la carpeta `bank/bank`:
```bash
mvn clean test
```
O desde la raíz del repositorio:
```bash
mvn -f bank/bank/pom.xml clean test
```

La rama `feature/completar-requisitos-pdf` contiene el trabajo en curso. La rama `main` se conserva separada hasta que la compilación, las pruebas y la revisión funcional estén completas.
