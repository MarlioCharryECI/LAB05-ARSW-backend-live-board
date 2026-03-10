package arsw.java.Live_Board.utils;

import arsw.java.Live_Board.model.StrokeDto;

/**
 * Utilidad centralizada para validaciones del BoardService.
 * Proporciona métodos estáticos para validar entradas y datos.
 */
public class BoardValidator {
    
    /**
     * Valida que un userId no sea nulo o vacío.
     *
     * @param userId ID del usuario a validar
     * @throws IllegalArgumentException si userId es inválido
     */
    public static void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId no puede ser nulo o vacío");
        }
    }
    
    /**
     * Valida que un trazo sea válido antes de procesarlo.
     *
     * @param stroke Trazo a validar
     * @param minStrokePoints Número mínimo de puntos requeridos
     * @throws IllegalArgumentException si el trazo es inválido
     */
    public static void validateStroke(StrokeDto stroke, int minStrokePoints) {
        if (stroke == null) {
            throw new IllegalArgumentException("El trazo no puede ser nulo");
        }
        if (stroke.points == null) {
            throw new IllegalArgumentException("Los puntos del trazo no pueden ser nulos");
        }
        if (stroke.points.size() < minStrokePoints) {
            throw new IllegalArgumentException(
                String.format("El trazo debe tener al menos %d puntos", minStrokePoints));
        }
        if (stroke.userId == null || stroke.userId.trim().isEmpty()) {
            throw new IllegalArgumentException("El userId del trazo no puede ser nulo o vacío");
        }
    }
}
