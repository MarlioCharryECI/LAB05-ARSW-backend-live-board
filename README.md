# LAB05-ARSW-backend-live-board

Backend para tablero colaborativo en tiempo real que permite a múltiples usuarios dibujar simultáneamente en un canvas compartido.

## Arquitectura

### Estrategia de Comunicación
Patrón Strategy que permite cambiar entre REST y WebSockets mediante configuración. Soporta operaciones REST incluso en modo WebSocket para compatibilidad total.

### WebSocket
- Gestión automática de sesiones
- Broadcasting condicional (solo si hay sesiones activas)
- Conexión en tiempo real a través de `ws://localhost:8080/ws?userId=uuid`

### Gestión de Estado
- `BoardService` centraliza el estado del tablero
- Estructuras concurrentes seguras para hilos
- Sistema de colores con paleta fija de 20 colores

## Endpoints

### Usuarios
- `POST /api/join` - Registro y asignación de color
- `POST /api/users/{userId}/heartbeat` - Mantener sesión activa

### Tablero
- `GET /api/board` - Obtener todos los trazos
- `POST /api/draw` - Agregar un trazo
- `DELETE /api/clear` - Limpiar el tablero
- `GET /api/board/sync?since=timestamp` - Sincronización

### WebSocket
- `ws://localhost:8080/ws?userId=uuid` - Conexión en tiempo real

## Tecnologías

- **Spring Boot** - Framework principal
- **Spring WebSocket** - Comunicación en tiempo real
- **Jackson** - Serialización JSON
- **SLF4J** - Logging estructurado
- **JUnit 5** - Testing

## Ejecución

```bash
mvn spring-boot:run
```

## Configuración

En `application.properties`:
```properties
board.communication.mode=websocket
board.user.timeout.minutes=5
logging.level.arsw.java.Live_Board=INFO
```

---

**Autor:** Marlio Jose Charry Espitia