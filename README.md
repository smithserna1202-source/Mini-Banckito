# Mini-Banckito

Proyecto académico de gestión bancaria desarrollado con Java 17, Spring Boot y Maven, organizado con una aproximación de arquitectura hexagonal.

## Estado de desarrollo

La implementación se está realizando en `feature/completar-requisitos-pdf`; `main` se conserva sin cambios mientras se completa y verifica el proyecto.

### Dominio implementado en esta etapa
- Dinero con `BigDecimal` y validación de montos.
- Clientes naturales, empresas y usuarios con roles.
- Cuentas con titular, tipo, moneda, fecha de apertura y estados.
- Controles para depósitos, retiros y transferencias internas.
- Solicitudes de préstamos, aprobación/rechazo por analista y reglas de desembolso.
- Transferencias empresariales sujetas a aprobación por umbral y vencimiento.
- Política central de permisos por rol.
- Modelo inmutable de auditoría y adaptador MongoDB de solo inserción.
- Pruebas unitarias adicionales y CI para ejecutar Maven.

## Requisitos
- JDK 17
- Maven 3.8+
- MongoDB local si se activa el perfil `mongodb`

## Ejecutar pruebas
```bash
mvn -f bank/bank/pom.xml test
```

La configuración de desarrollo incluye H2 para persistencia relacional futura y MongoDB para la bitácora. Las entidades/adaptadores SQL, autenticación, autorización HTTP y controladores deben completarse y verificarse antes de considerar el sistema integral.

Este repositorio es académico y no debe utilizarse para administrar dinero real. Un despliegue requiere controles de seguridad, transacciones de base de datos, autenticación, autorización por endpoint y pruebas de seguridad.
