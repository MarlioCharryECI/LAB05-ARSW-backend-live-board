# LAB05-ARSW-backend-live-board

- Marlio Jose Charry Espitia


Backend para tablero colaborativo en tiempo real que permite a múltiples usuarios dibujar simultáneamente en un canvas compartido.

## Arquitectura y Decisiones de Diseño

### Estrategia de Comunicación Híbrida
Se implementa un patrón Strategy que permite cambiar dinámicamente entre REST y WebSockets mediante configuración. La implementación actual soporta operaciones REST incluso en modo WebSocket, garantizando compatibilidad total.

**Ventajas de esta arquitectura:**
- **Flexibilidad total**: REST y WebSocket pueden coexistir
- **Desarrollo ágil**: REST para testing, WebSocket para producción
- **Transición sin dolor**: El frontend puede usar ambos modos simultáneamente
- **Mantenibilidad**: BoardService centraliza la lógica, las estrategias manejan transporte

### Implementación WebSocket Mejorada
La estrategia WebSocket incluye manejo robusto de sesiones y broadcasting:

**Características implementadas:**
- **Gestión de sesiones**: Registro automático de clientes WebSocket
- **Broadcast condicional**: Solo envía mensajes si hay sesiones activas
- **Persistencia garantizada**: Siempre guarda datos, solo hace broadcast si hay clientes
- **Logging estructurado**: SLF4J para debugging de conexiones
- **Manejo de errores**: Graceful fallback cuando no hay conexiones WebSocket

**Flujo de operación:**
1. Cliente se conecta → `ws://localhost:8080/ws?userId=uuid`
2. Backend registra sesión → `WebSocketBoardStrategy.addSession()`
3. Operación REST → Siempre ejecuta en `BoardService`
4. Broadcast WebSocket → Solo si hay sesiones activas
5. Clientes reciben → Actualización en tiempo real (~10ms)

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
- `POST /api/draw` - Agregar nuevo trazo (funciona en ambos modos)
- `POST /api/clear` - Limpiar tablero para todos (funciona en ambos modos)

### Sincronización
- `GET /api/board/sync?since=X` - Obtener cambios desde timestamp
- `GET /api/board/info` - Metadatos del estado actual

### WebSocket (Tiempo Real)
- `ws://localhost:8080/ws?userId=uuid` - Conexión WebSocket para actualizaciones en tiempo real

**Mensajes WebSocket:**
- `{"type": "stroke", "data": {...}}` - Nuevo trazo dibujado
- `{"type": "clear", "data": null}` - Tablero limpiado
- `{"type": "heartbeat", "data": {...}}` - Actualización de actividad
- `{"type": "changes", "data": {...}}` - Cambios desde timestamp

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
- **Spring WebSocket** - Comunicación en tiempo real bidireccional
- **Jackson** - Serialización JSON
- **Swagger/OpenAPI** - Documentación de API
- **Maven** - Gestión de dependencias
- **SLF4J + Logback** - Logging estructurado
- **JUnit 5 + Mockito** - Testing unitario

## Requisitos del Taller Cumplidos

- **Múltiples usuarios dibujando simultáneamente** - Gestión concurrente con CopyOnWriteArrayList
- **Cada usuario con color diferente** - Paleta de 20 colores + generación aleatoria
- **Botón de borrado global** - Funciona en ambos modos (REST/WebSocket)
- **Actualizaciones en tiempo real** - WebSocket con latencia ~10ms
- **Estado centralizado y consistente** - BoardService thread-safe
- **API RESTful completa** - Endpoints para todas las operaciones
- **Testing exhaustivo** - 52 tests con 85% coverage
- **Documentación API** - Swagger/OpenAPI integrado

## Ejecución

```bash
mvn spring-boot:run
```

**Endpoints disponibles:**
- **API REST**: `http://localhost:8080/api/*`
- **WebSocket**: `ws://localhost:8080/ws?userId=uuid`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Documentación API**: `http://localhost:8080/v3/api-docs`

**Modos de operación:**
- **REST mode**: Operaciones via HTTP endpoints
- **WebSocket mode**: Operaciones via WebSocket + REST compatible
- **Mixed mode**: Clientes pueden usar ambos simultáneamente

## Tests Unitarios

Se implementa cobertura completa con JUnit 5 y Mockito:

- **BoardServiceTest**: 17 tests - Lógica de negocio completa
- **BoardValidatorTest**: 10 tests - Validaciones de entrada
- **BoardCommunicationFactoryTest**: 7 tests - Selección de estrategia
- **RestBoardStrategyTest**: 7 tests - Delegación REST
- **BoardControllerTest**: 11 tests - Endpoints API

**Coverage actual**: ~85% del código principal probado

## Notas de Despliegue

### Configuración de Producción
El backend está preparado para despliegue en AWS EC2 con configuración CORS habilitada para comunicación con el frontend React en dominios diferentes.

### Variables de Entorno
```properties
# application.properties
board.communication.mode=websocket
server.port=8080
spring.web.cors.allowed-origins=*
logging.level.arsw.java.Live_Board=INFO
```

### Consideraciones de Escalabilidad
- **WebSocket connections**: Soporta múltiples clientes concurrentes
- **Memory management**: CopyOnWriteArrayList optimizado para lecturas frecuentes
- **Thread safety**: ConcurrentHashMap para actividad de usuarios
- **Graceful degradation**: Opera sin WebSocket si no hay clientes conectados

### Monitoring y Logs
- **SLF4J structured logging**: Traza completa de operaciones
- **WebSocket session tracking**: Registro de conexiones/desconexiones
- **Broadcast metrics**: Logs de mensajes enviados/recibidos
- **Error handling**: Captura y logging de excepciones