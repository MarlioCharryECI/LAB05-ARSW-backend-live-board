package arsw.java.Live_Board.Controllers;

import arsw.java.Live_Board.Services.BoardService;
import arsw.java.Live_Board.model.JoinRequest;
import arsw.java.Live_Board.model.JoinResponse;
import arsw.java.Live_Board.model.StrokeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Board", description = "Operaciones del tablero")
public class BoardController {

    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
    private final BoardService service;

    public BoardController(BoardService service) {
        this.service = service;
    }


    @Operation(summary = "Unirse y obtener color único")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario unido exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping("/join")
    public ResponseEntity<JoinResponse> join(@Valid @RequestBody JoinRequest req) {
        logger.info("Usuario intentando unirse: {}", req.userId);
        JoinResponse response = service.join(req.userId);
        logger.info("Usuario {} unido con color: {}", req.userId, response.color);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener todos los trazos del tablero")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trazos obtenidos exitosamente")
    })
    @GetMapping("/board")
    public ResponseEntity<List<StrokeDto>> board() {
        logger.debug("Obteniendo trazos del tablero");
        List<StrokeDto> strokes = service.getBoard();
        logger.debug("Retornando {} trazos", strokes.size());
        return ResponseEntity.ok(strokes);
    }

    @Operation(summary = "Agregar un trazo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Trazo agregado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del trazo inválidos")
    })
    @PostMapping("/draw")
    public ResponseEntity<Void> draw(@Valid @RequestBody StrokeDto stroke) {
        logger.info("Agregando trazo del usuario: {}", stroke.userId);
        service.addStroke(stroke);
        logger.debug("Trazo agregado exitosamente");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Borrar el tablero")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tablero borrado exitosamente")
    })
    @PostMapping("/clear")
    public ResponseEntity<Void> clear() {
        logger.info("Borrando tablero");
        service.clear();
        logger.info("Tablero borrado exitosamente");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener cambios desde un timestamp específico (para polling)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cambios obtenidos exitosamente")
    })
    @GetMapping("/board/sync")
    public ResponseEntity<Map<String, Object>> syncBoard(
            @RequestParam(required = false) Long since) {
        logger.debug("Sincronizando cambios desde timestamp: {}", since);
        Map<String, Object> syncData = service.getChangesSince(since);
        logger.debug("Retornando {} trazos modificados", 
                    ((List<?>) syncData.get("strokes")).size());
        return ResponseEntity.ok(syncData);
    }

    @Operation(summary = "Heartbeat para mantener sesión activa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sesion actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping("/users/{userId}/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(@PathVariable String userId) {
        logger.debug("Heartbeat para usuario: {}", userId);
        Map<String, Object> response = service.updateUserActivity(userId);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            logger.warn("Usuario {} no encontrado para heartbeat", userId);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Obtener información del tablero para sincronización")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Información obtenida exitosamente")
    })
    @GetMapping("/board/info")
    public ResponseEntity<Map<String, Object>> getBoardInfo() {
        logger.debug("Obteniendo información del tablero");
        Map<String, Object> info = service.getBoardInfo();
        return ResponseEntity.ok(info);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Error de validación: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        logger.error("Error inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
    }
}
