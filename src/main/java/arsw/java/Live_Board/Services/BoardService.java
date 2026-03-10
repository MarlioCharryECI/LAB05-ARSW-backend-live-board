package arsw.java.Live_Board.Services;
import arsw.java.Live_Board.model.JoinResponse;
import arsw.java.Live_Board.model.StrokeDto;
import arsw.java.Live_Board.utils.BoardLogger;
import arsw.java.Live_Board.utils.BoardValidator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private static final class Config {
        static final int RGB_MAX = 0xFFFFFF;
        static final int MIN_STROKE_POINTS = 1;
        static final long USER_TIMEOUT_MS = 300000; // 5 minutos

        static final String[] COLOR_PALETTE = {
            "#e6194b", "#3cb44b", "#ffe119", "#4363d8", "#f58231", "#911eb4", "#46f0f0",
            "#f032e6", "#bcf60c", "#fabebe", "#008080", "#e6beff", "#9a6324", "#fffac8",
            "#800000", "#aaffc3", "#808000", "#ffd8b1", "#000075", "#808080"
        };
    }
    
    private final List<StrokeDto> strokes = new CopyOnWriteArrayList<>();
    private final Map<String, String> userColors = new HashMap<>();
    private final Map<String, Long> userLastActivity = new ConcurrentHashMap<>();
    private final Deque<String> palette = new ArrayDeque<>(List.of(Config.COLOR_PALETTE));
    private final Random rnd = new Random();
    private volatile long boardVersion = 0;
    private volatile long lastModified = System.currentTimeMillis();

    /**
     * Registra un usuario en el tablero y le asigna un color único.
     * Si el usuario ya existe, retorna su color actual.
     *
     * @param userId ID del usuario a registrar (no debe ser nulo o vacío)
     * @return JoinResponse con el userId y color asignado
     * @throws IllegalArgumentException si userId es nulo o vacío
     */
    public synchronized JoinResponse join(String userId) {
        BoardValidator.validateUserId(userId);
        
        BoardLogger.logUserJoining(userId);
        
        String color = userColors.get(userId);
        if (color == null) {
            color = palette.isEmpty() ? generateRandomColor() : palette.pop();
            userColors.put(userId, color);
            userLastActivity.put(userId, System.currentTimeMillis());
            BoardLogger.logNewColorAssigned(userId, color);
        } else {
            userLastActivity.put(userId, System.currentTimeMillis());
            BoardLogger.logUserExists(userId, color);
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
        BoardLogger.logBoardStrokesCount(strokes.size());
        return Collections.unmodifiableList(new ArrayList<>(strokes));
    }

    /**
     * Agrega un nuevo trazo al tablero después de validarlo.
     *
     * @param stroke Trazo a agregar (no debe ser nulo)
     * @throws IllegalArgumentException si el trazo o sus puntos son inválidos
     */
    public void addStroke(StrokeDto stroke) {
        BoardValidator.validateStroke(stroke, Config.MIN_STROKE_POINTS);
        
        BoardLogger.logStrokeAdded(stroke.userId, stroke.points.size());
        
        strokes.add(stroke);
        boardVersion++;
        lastModified = System.currentTimeMillis();
        userLastActivity.put(stroke.userId, System.currentTimeMillis());
        BoardLogger.logBoardStrokesTotal(strokes.size());
    }

    /**
     * Limpia todos los trazos del tablero.
     */
    public void clear() {
        int strokeCount = strokes.size();
        strokes.clear();
        boardVersion++;
        lastModified = System.currentTimeMillis();
        BoardLogger.logBoardCleared(strokeCount);
    }
    
    /**
     * Genera un color hexadecimal aleatorio.
     *
     * @return Color en formato #RRGGBB
     */
    private String generateRandomColor() {
        return String.format("#%06x", rnd.nextInt(Config.RGB_MAX));
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
        stats.put("boardVersion", boardVersion);
        stats.put("lastModified", lastModified);
        return stats;
    }
    
    /**
     * Obtiene cambios desde un timestamp específico para sincronización.
     *
     * @param since Timestamp desde el cual buscar cambios (null para obtener todos)
     * @return Map con trazos modificados y metadata
     */
    public Map<String, Object> getChangesSince(Long since) {
        Map<String, Object> result = new HashMap<>();
        
        List<StrokeDto> changedStrokes;
        if (since == null) {
            changedStrokes = new ArrayList<>(strokes);
        } else {
            changedStrokes = strokes.stream()
                    .filter(stroke -> strokeAddedAfter(stroke, since))
                    .collect(Collectors.toList());
        }
        
        result.put("strokes", changedStrokes);
        result.put("boardVersion", boardVersion);
        result.put("lastModified", lastModified);
        result.put("timestamp", System.currentTimeMillis());

        cleanupInactiveUsers();
        
        return result;
    }
    
    /**
     * Actualiza la actividad de un usuario (heartbeat).
     *
     * @param userId ID del usuario
     * @return Map con información del usuario o null si no existe
     */
    public Map<String, Object> updateUserActivity(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        String color = userColors.get(userId);
        if (color != null) {
            userLastActivity.put(userId, System.currentTimeMillis());
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", userId);
            userInfo.put("color", color);
            userInfo.put("lastActivity", userLastActivity.get(userId));
            userInfo.put("boardVersion", boardVersion);
            
        BoardLogger.logUserActivityUpdated(userId);
            return userInfo;
        }
        
        return new HashMap<>();
    }
    
    /**
     * Obtiene información del tablero para sincronización.
     *
     * @return Map con información del estado actual
     */
    public Map<String, Object> getBoardInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("boardVersion", boardVersion);
        info.put("lastModified", lastModified);
        info.put("totalStrokes", strokes.size());
        info.put("activeUsers", userColors.size());
        info.put("availableColors", palette.size());
        info.put("timestamp", System.currentTimeMillis());

        cleanupInactiveUsers();
        
        return info;
    }
    
    /**
     * Limpia usuarios inactivos basado en timeout.
     */
    private void cleanupInactiveUsers() {
        long currentTime = System.currentTimeMillis();
        List<String> inactiveUsers = new ArrayList<>();
        
        userLastActivity.forEach((userId, lastActivity) -> {
            if (currentTime - lastActivity > Config.USER_TIMEOUT_MS) {
                inactiveUsers.add(userId);
            }
        });
        
        for (String userId : inactiveUsers) {
            removeUser(userId);
        }
        
        if (!inactiveUsers.isEmpty()) {
            BoardLogger.logInactiveUsersCleaned(inactiveUsers.size());
        }
    }
    
    /**
     * Remueve un usuario del tablero y libera su color.
     *
     * @param userId ID del usuario a remover
     */
    private void removeUser(String userId) {
        String color = userColors.remove(userId);
        if (color != null) {
            palette.addLast(color);
        }
        userLastActivity.remove(userId);
        BoardLogger.logUserRemoved(userId);
    }
    
    /**
     * Verifica si un trazo fue agregado después de un timestamp.
     *
     * @param stroke Trazo a verificar
     * @param timestamp Timestamp de referencia
     * @return true si fue agregado después
     */
    private boolean strokeAddedAfter(StrokeDto stroke, Long timestamp) {
        // Implementación simplificada: se asume que todos los trazos son "nuevos"
        return true;
    }
}
