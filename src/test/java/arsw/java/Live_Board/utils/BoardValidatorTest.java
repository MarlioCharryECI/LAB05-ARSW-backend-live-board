package arsw.java.Live_Board.utils;

import arsw.java.Live_Board.model.PointDto;
import arsw.java.Live_Board.model.StrokeDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardValidatorTest {

    @Test
    void validateUserId_ValidUserId_ShouldNotThrow() {
        assertDoesNotThrow(() -> BoardValidator.validateUserId("usuario1"));
        assertDoesNotThrow(() -> BoardValidator.validateUserId("user123"));
        assertDoesNotThrow(() -> BoardValidator.validateUserId("a"));
    }

    @Test
    void validateUserId_NullUserId_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BoardValidator.validateUserId(null)
        );
        assertEquals("userId no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void validateUserId_EmptyUserId_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BoardValidator.validateUserId("")
        );
        assertEquals("userId no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void validateUserId_WhitespaceOnlyUserId_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BoardValidator.validateUserId("   ")
        );
        assertEquals("userId no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void validateStroke_ValidStroke_ShouldNotThrow() {
        StrokeDto stroke = new StrokeDto();
        stroke.userId = "usuario1";
        stroke.points = List.of(createPoint(10, 20), createPoint(30, 40));
        stroke.color = "#FF0000";

        assertDoesNotThrow(() -> BoardValidator.validateStroke(stroke, 1));
    }

    @Test
    void validateStroke_NullStroke_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BoardValidator.validateStroke(null, 1)
        );
        assertEquals("El trazo no puede ser nulo", exception.getMessage());
    }

    @Test
    void validateStroke_NullPoints_ShouldThrowException() {
        StrokeDto stroke = new StrokeDto();
        stroke.userId = "usuario1";
        stroke.points = null;

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BoardValidator.validateStroke(stroke, 1)
        );
        assertEquals("Los puntos del trazo no pueden ser nulos", exception.getMessage());
    }

    @Test
    void validateStroke_NotEnoughPoints_ShouldThrowException() {
        StrokeDto stroke = new StrokeDto();
        stroke.userId = "usuario1";
        stroke.points = List.of(createPoint(10, 20));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BoardValidator.validateStroke(stroke, 2)
        );
        assertEquals("El trazo debe tener al menos 2 puntos", exception.getMessage());
    }

    @Test
    void validateStroke_EmptyUserId_ShouldThrowException() {
        StrokeDto stroke = new StrokeDto();
        stroke.userId = "";
        stroke.points = List.of(createPoint(10, 20), createPoint(30, 40));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BoardValidator.validateStroke(stroke, 1)
        );
        assertEquals("El userId del trazo no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void validateStroke_NullUserId_ShouldThrowException() {
        StrokeDto stroke = new StrokeDto();
        stroke.userId = null;
        stroke.points = List.of(createPoint(10, 20), createPoint(30, 40));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BoardValidator.validateStroke(stroke, 1)
        );
        assertEquals("El userId del trazo no puede ser nulo o vacío", exception.getMessage());
    }

    private PointDto createPoint(double x, double y) {
        PointDto point = new PointDto();
        point.x = x;
        point.y = y;
        return point;
    }
}
