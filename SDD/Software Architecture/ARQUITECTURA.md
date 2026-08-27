# Software Architecture - Mini-Banckito

## Overview

The **Mini-Banckito** system uses a **Hexagonal Architecture (Ports and Adapters)**. 

The main goal is to keep the banking business rules (like depositing, withdrawing, and calculating balances) protected in the center, completely separated from external tools like user interfaces or databases.

---

## Architectural Principles

- **Domain-First:** The business rules are the most important part of the code.
- **Separation of Concerns:** Each part of the code has only one job.
- **Independence:** The core banking logic does not depend on external frameworks.

---

# Architecture Layers

The application is divided into four main layers:

```text
src/
└── main/
    └── java/
        └── application/
            │
            ├── App.java            <-- (El Botón de Encendido)
            │
            ├── adapters/           <-- (Los Traductores)
            │   ├── in/             <-- Lo que entra (pantalla / usuarios)
            │   └── out/            <-- Lo que sale (guardar en base de datos)
            │
            ├── domain/             <-- (El Cerebro / Las Reglas del Banco)
            │   ├── models/         <-- Las cosas del banco (Cuenta, Cliente)
            │   ├── services/       <-- Lo que el banco sabe hacer (Transferir)
            │   └── ports/          <-- Las puertas de conexión
            │
            └── infrastructure/     <-- (Las Herramientas Técnicas)
            