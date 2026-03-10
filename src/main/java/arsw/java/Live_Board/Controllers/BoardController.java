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
