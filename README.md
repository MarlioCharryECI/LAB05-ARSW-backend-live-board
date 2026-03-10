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

### Despliegue en Producción (Azure)

#### Configuración Automática
El sistema está configurado para despliegue automático en Azure Web Apps mediante GitHub Actions:

**Backend:**
1. **Web App**: Live-Board-Backend
2. **URL Backend**: https://live-board-backend-hfhff6b0f6cvf3h8.westus3-01.azurewebsites.net
3. **Implementación**: Automática desde rama main del repositorio GitHub
4. **Runtime**: Java 21 SE en Linux

**Frontend:**
1. **Web App**: Live-Board-Frontend
2. **URL Frontend**: https://live-board-fcb6a0eedtdcgfh6.westus3-01.azurewebsites.net
3. **Implementación**: Despliegue estático de React en Azure Web Apps
4. **Runtime**: Node.js sobre Linux

#### Configuración de Aplicación
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

# Puerto dinámico para Azure
server.port=8080
```

### Endpoints Disponibles

#### Entorno de Desarrollo
- **API REST**: http://localhost:8080/api/
- **WebSocket**: ws://localhost:8080/ws
- **Documentación Swagger**: http://localhost:8080/swagger-ui.html
- **Frontend Local**: http://localhost:5173

#### Entorno de Producción (Azure)
- **Backend API**: https://live-board-backend-hfhff6b0f6cvf3h8.westus3-01.azurewebsites.net/api/
- **Backend WebSocket**: wss://live-board-backend-hfhff6b0f6cvf3h8.westus3-01.azurewebsites.net/ws
- **Backend Swagger**: https://live-board-backend-hfhff6b0f6cvf3h8.westus3-01.azurewebsites.net/swagger-ui.html
- **Frontend Producción**: https://live-board-fcb6a0eedtdcgfh6.westus3-01.azurewebsites.net/

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
- **Orígenes permitidos**:
  - Desarrollo local: http://localhost:5173 (React)
  - Pruebas locales: http://localhost:8080 (backend directo)
  - Producción: https://live-board-fcb6a0eedtdcgfh6.westus3-01.azurewebsites.net
- **Métodos HTTP**: GET, POST, PUT, DELETE, OPTIONS
- **Headers**: Permitidos todos los headers necesarios

### Configuración Azure
Para producción en Azure Web Apps:
- **Runtime**: Java 21 SE sobre Linux
- **Plan**: Free (F1) escalable según demanda
- **Implementación**: Automática desde GitHub
- **Dominio**: live-board-backend-hfhff6b0f6cvf3h8.westus3-01.azurewebsites.net

### Monitoreo y Logging
- Sistema de logging estructurado con niveles apropiados (INFO/WARN/ERROR)
- Logs de conexión y desconexión WebSocket
- Registro de operaciones críticas del tablero
- Logs de implementación disponibles en Azure Portal

### Verificación de Funcionamiento
Para verificar que el sistema funciona correctamente en producción:

#### Backend
1. **API REST**: https://live-board-backend-hfhff6b0f6cvf3h8.westus3-01.azurewebsites.net/api/board
2. **Documentación Swagger**: https://live-board-backend-hfhff6b0f6cvf3h8.westus3-01.azurewebsites.net/swagger-ui.html
3. **Logs**: Azure Portal → Web App → Registros

#### Frontend
1. **Aplicación completa**: https://live-board-fcb6a0eedtdcgfh6.westus3-01.azurewebsites.net/
2. **Conexión WebSocket**: Verificar comunicación en tiempo real entre múltiples usuarios
3. **Funcionalidades**: Dibujado simultáneo, colores únicos, borrado global

---

**Autor:** Marlio Jose Charry Espitia  
**Versión:** 1.0.0  
**Última actualización:** Marzo 2026