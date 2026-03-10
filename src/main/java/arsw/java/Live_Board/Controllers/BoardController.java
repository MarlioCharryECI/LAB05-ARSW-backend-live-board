package arsw.java.Live_Board.Controllers;

import arsw.java.Live_Board.Services.BoardService;
import arsw.java.Live_Board.model.JoinRequest;
import arsw.java.Live_Board.model.JoinResponse;
import arsw.java.Live_Board.model.StrokeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Board", description = "Operaciones del tablero")
public class BoardController {

    private final BoardService service;

    public BoardController(BoardService service) {
        this.service = service;
    }


    @Operation(summary = "Unirse y obtener color único")
    @PostMapping("/join")
    public JoinResponse join(@RequestBody JoinRequest req) {
        if (req == null || req.userId == null || req.userId.isBlank()) {
            throw new IllegalArgumentException("userId requerido");
        }
        return service.join(req.userId);
    }

    @Operation(summary = "Obtener todos los trazos del tablero")
    @GetMapping("/board")
    public List<StrokeDto> board() {
        return service.getBoard();
    }

    @Operation(summary = "Agregar un trazo")
    @PostMapping("/draw")
    public void draw(@RequestBody StrokeDto stroke) {
        service.addStroke(stroke);
    }

    @Operation(summary = "Borrar el tablero")
    @PostMapping("/clear")
    public void clear() {
        service.clear();
    }
}
