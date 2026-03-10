# LAB05-ARSW-backend-live-board

Backend para tablero colaborativo en tiempo real que permite a múltiples usuarios dibujar simultáneamente en un canvas compartido.

## Arquitectura del Sistema

### Estrategia de Comunicación Híbrida
El sistema implementa un patrón Strategy que permite la selección dinámica entre REST y WebSockets mediante configuración externa. Esta arquitectura garantiza compatibilidad total entre modos de comunicación.

### Implementación WebSocket
La estrategia WebSocket proporciona comunicación bidireccional con las siguientes características:
- Gestión automática de sesiones de clientes
- Broadcasting condicional basado en sesiones activas
- Conexión en tiempo real mediante endpoint `ws://localhost:8080/ws?userId=uuid`

### Gestión de Estado Centralizada
El componente BoardService centraliza toda la lógica de estado del tablero:
- Almacenamiento thread-safe de trazos mediante CopyOnWriteArrayList
- Gestión concurrente de actividad de usuarios con ConcurrentHashMap
- Sistema de asignación de colores con paleta predefinida de 20 colores

## API REST

### Gestión de Usuarios
- `POST /api/join` - Registro de usuario y asignación de color único
- `POST /api/users/{userId}/heartbeat` - Mantenimiento de sesión activa

### Operaciones del Tablero
- `GET /api/board` - Obtención completa de trazos almacenados
- `POST /api/draw` - Incorporación de nuevo trazo al tablero
- `DELETE /api/clear` - Limpieza global del tablero
- `GET /api/board/sync?since=timestamp` - Sincronización incremental

### Comunicación WebSocket
- Endpoint: `ws://localhost:8080/ws?userId=uuid`
- Mensajes soportados: stroke, clear, heartbeat, changes

## Stack Tecnológico

### Frameworks y Librerías
- **Spring Boot 4.0.3** - Framework principal de aplicación
- **Spring WebSocket** - Comunicación bidireccional en tiempo real
- **Jackson** - Serialización y deserialización JSON
- **SLF4J con Logback** - Sistema de logging estructurado
- **JUnit 5** - Framework de testing unitario
- **Mockito** - Framework para mocking en pruebas

### Herramientas de Desarrollo
- **Maven** - Gestión de dependencias y ciclo de vida
- **Spotless** - Formateo automático de código con Google Java Format
- **Swagger/OpenAPI** - Documentación automática de API

## Ejecución y Despliegue

### Entorno de Desarrollo
```bash
# Compilación y ejecución
mvn clean compile
mvn spring-boot:run

# Aplicar formateo de código
mvn spotless:apply

# Validación de formato
mvn spotless:check
```

### Configuración de Aplicación
El sistema utiliza configuración externa mediante `application.properties`:

```properties
# Modo de comunicación: rest o websocket
board.communication.mode=websocket

# Timeout de inactividad de usuarios (minutos)
board.user.timeout.minutes=5

# Nivel de logging: INFO, WARN, ERROR
logging.level.arsw.java.Live_Board=INFO

# Configuración de serialización JSON
spring.jackson.serialization.indent-output=true
```

### Endpoints Disponibles
- **API REST**: http://localhost:8080/api/
- **WebSocket**: ws://localhost:8080/ws
- **Documentación Swagger**: http://localhost:8080/swagger-ui.html

## Arquitectura de Testing

### Estrategia de Pruebas
El proyecto implementa una estrategia completa de testing que incluye:
- **Tests Unitarios**: Validación de lógica de negocio individual
- **Tests de Integración**: Verificación de interacción entre componentes
- **Tests de Controller**: Validación de endpoints REST

### Cobertura de Pruebas
- BoardService: Lógica central del tablero
- BoardCommunicationFactory: Selección de estrategias
- RestBoardStrategy y WebSocketBoardStrategy: Modos de comunicación
- BoardController: Endpoints REST y validación

## Consideraciones de Producción

### Configuración CORS
El sistema incluye configuración CORS específica para comunicación con frontend en dominios diferentes:
- Orígenes permitidos configurados para desarrollo local
- Métodos HTTP restringidos a operaciones necesarias
- Headers específicos para seguridad

### Monitoreo y Logging
- Sistema de logging estructurado con niveles apropiados
- Logs de conexión y desconexión WebSocket
- Registro de operaciones críticas del tablero

---

**Autor:** Marlio Jose Charry Espitia  
**Versión:** 1.0.0  
**Última actualización:** Marzo 2026