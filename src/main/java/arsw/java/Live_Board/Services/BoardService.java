package arsw.java.Live_Board.Services;
import arsw.java.Live_Board.model.JoinResponse;
import arsw.java.Live_Board.model.StrokeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BoardService {

    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);
    private static final int RGB_MAX = 0xFFFFFF;
    private static final int MIN_STROKE_POINTS = 1;
    
    private final List<StrokeDto> strokes = new CopyOnWriteArrayList<>();
    private final Map<String, String> userColors = new HashMap<>();
    private final Deque<String> palette = new ArrayDeque<>(List.of(
            "#e6194b","#3cb44b","#ffe119","#4363d8","#f58231","#911eb4","#46f0f0",
            "#f032e6","#bcf60c","#fabebe","#008080","#e6beff","#9a6324","#fffac8",
            "#800000","#aaffc3","#808000","#ffd8b1","#000075","#808080"
    ));
    private final Random rnd = new Random();

    /**
     * Registra un usuario en el tablero y le asigna un color único.
     * Si el usuario ya existe, retorna su color actual.
     *
     * @param userId ID del usuario a registrar (no debe ser nulo o vacío)
     * @return JoinResponse con el userId y color asignado
     * @throws IllegalArgumentException si userId es nulo o vacío
     */
    public synchronized JoinResponse join(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId no puede ser nulo o vacío");
        }
        
        logger.info("Usuario intentando unirse: {}", userId);
        
        String color = userColors.get(userId);
        if (color == null) {
            color = palette.isEmpty() ? generateRandomColor() : palette.pop();
            userColors.put(userId, color);
            logger.info("Nuevo color asignado al usuario {}: {}", userId, color);
        } else {
            logger.debug("Usuario {} ya existe con color: {}", userId, color);
        }
        
        JoinResponse resp = new JoinResponse();
        resp.userId = userId;
        resp.color = color;
        return resp;
    }

    /**
     * Obtiene una copia inmutable de todos los trazos del tablero.
     *
     * @return Lista inmutable de trazos
     */
    public List<StrokeDto> getBoard() {
        logger.debug("Obteniendo {} trazos del tablero", strokes.size());
        return Collections.unmodifiableList(new ArrayList<>(strokes));
    }

    /**
     * Agrega un nuevo trazo al tablero después de validarlo.
     *
     * @param stroke Trazo a agregar (no debe ser nulo)
     * @throws IllegalArgumentException si el trazo o sus puntos son inválidos
     */
    public void addStroke(StrokeDto stroke) {
        validateStroke(stroke);
        
        logger.debug("Agregando trazo del usuario: {} con {} puntos", 
                    stroke.userId, stroke.points.size());
        
        strokes.add(stroke);
        logger.debug("Total de trazos en el tablero: {}", strokes.size());
    }

    /**
     * Limpia todos los trazos del tablero.
     */
    public void clear() {
        int strokeCount = strokes.size();
        strokes.clear();
        logger.info("Tablero limpiado. Se eliminaron {} trazos", strokeCount);
    }
    
    /**
     * Genera un color hexadecimal aleatorio.
     *
     * @return Color en formato #RRGGBB
     */
    private String generateRandomColor() {
        return String.format("#%06x", rnd.nextInt(RGB_MAX));
    }
    
    /**
     * Valida que un trazo sea válido antes de agregarlo.
     *
     * @param stroke Trazo a validar
     * @throws IllegalArgumentException si el trazo es inválido
     */
    private void validateStroke(StrokeDto stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("El trazo no puede ser nulo");
        }
        if (stroke.points == null) {
            throw new IllegalArgumentException("Los puntos del trazo no pueden ser nulos");
        }
        if (stroke.points.size() < MIN_STROKE_POINTS) {
            throw new IllegalArgumentException(
                String.format("El trazo debe tener al menos %d puntos", MIN_STROKE_POINTS));
        }
        if (stroke.userId == null || stroke.userId.trim().isEmpty()) {
            throw new IllegalArgumentException("El userId del trazo no puede ser nulo o vacío");
        }
    }
    
    /**
     * Obtiene estadísticas del tablero.
     *
     * @return Map con estadísticas (trazos, usuarios activos)
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStrokes", strokes.size());
        stats.put("activeUsers", userColors.size());
        stats.put("availableColors", palette.size());
        return stats;
    }
}
