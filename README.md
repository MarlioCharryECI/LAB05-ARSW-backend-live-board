# LAB05-ARSW-backend-live-board

Backend para tablero colaborativo en tiempo real que permite a múltiples usuarios dibujar simultáneamente en un canvas compartido.

## Arquitectura y Decisiones de Diseño

### Estrategia de Comunicación Dual
Se implementa un patrón Strategy que permite cambiar dinámicamente entre REST y WebSockets mediante configuración. Esta decisión resuelve el requisito del taller de permitir actualizaciones en tiempo real mientras mantiene la flexibilidad de desarrollo.

**Por qué esta arquitectura:**
- **Facilidad de testing**: REST es más simple para depurar durante desarrollo
- **Producción lista**: WebSockets ofrece verdadera comunicación en tiempo real
- **Transición sin dolor**: El frontend no necesita saber qué modo usa el backend
- **Mantenibilidad**: La lógica de negocio permanece en BoardService, independientemente del transporte

### Gestión de Estado Centralizada
BoardService maneja todo el estado del tablero usando estructuras concurrentes seguras para hilos. Esto es crucial porque múltiples usuarios pueden dibujar simultáneamente.

**Decisiones clave:**
- `CopyOnWriteArrayList` para trazos: Lecturas frecuentes, escrituras ocasionales
- `ConcurrentHashMap` para actividad de usuarios: Actualizaciones concurrentes de heartbeats
- Sincronización en `join()`: Evita condiciones de carrera al asignar colores

### Sistema de Colores
Implementamos una paleta fija de 20 colores con asignación determinista. Cuando se agotan, generamos colores aleatorios. Esto asegura que cada usuario tenga un color visualmente distinguible, cumpliendo con el requisito del taller.

### Validación y Logging Centralizados
Creamos utilidades separadas (BoardValidator, BoardLogger) para mantener el código limpio y facilitar el mantenimiento. Esto sigue principios SOLID de responsabilidad única.

### Configuración Externa
Todas las decisiones operativas (timeout de usuarios, modo de comunicación) están en `application.properties`. Esto permite despliegue flexible en diferentes entornos sin recompilar.

## Endpoints Principales

### Gestión de Usuarios
- `POST /api/join` - Registro y asignación de color único
- `POST /api/users/{userId}/heartbeat` - Mantener sesión activa

### Operaciones del Tablero
- `GET /api/board` - Obtener todos los trazos
- `POST /api/draw` - Agregar nuevo trazo
- `POST /api/clear` - Limpiar tablero para todos

### Sincronización
- `GET /api/board/sync?since=X` - Obtener cambios desde timestamp
- `GET /api/board/info` - Metadatos del estado actual

## Configuración de Modo de Comunicación

En `application.properties`:
```properties
# Para desarrollo/desarrollo
board.communication.mode=rest

# Para producción/tiempo real
board.communication.mode=websocket
```

## Tecnologías

- **Spring Boot 4.0.3** - Framework principal
- **WebSockets** - Comunicación en tiempo real
- **Jackson** - Serialización JSON
- **Swagger/OpenAPI** - Documentación de API
- **Maven** - Gestión de dependencias

## Requisitos del Taller Cumplidos

- Múltiples usuarios dibujando simultáneamente
- Cada usuario con color diferente
- Botón de borrado global
- Actualizaciones en tiempo real (modo WebSocket)
- Estado centralizado y consistente
- API RESTful para integración con frontend React

## Ejecución

```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080` con Swagger UI en `http://localhost:8080/swagger-ui.html`.

## Notas de Despliegue

El backend está preparado para despliegue en AWS EC2 con configuración CORS habilitada para comunicación con el frontend React en dominios diferentes.