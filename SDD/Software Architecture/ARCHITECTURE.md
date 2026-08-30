# Software Architecture - Mini-Banckito

## Hexagonal Architecture Overview
This application follows the **Hexagonal Architecture (Ports and Adapters)** pattern to maintain a strict separation between business logic and external infrastructure.

### Layers:
1. **Domain (`application.domain`)**: Contains core business logic, entities, value objects, domain services, and repository ports.
2. **Adapters (`application.adapters`)**: Implements repository interfaces using in-memory data structures.
3. **Infrastructure (`application.infrastructure`)**: Handles startup configuration, console application drivers, and entry points.

