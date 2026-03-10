package arsw.java.Live_Board.factory;

import arsw.java.Live_Board.strategy.BoardCommunicationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoardCommunicationFactoryTest {

    @Mock
    private Environment environment;

    private BoardCommunicationFactory factory;
    private Map<String, BoardCommunicationStrategy> mockStrategies;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        factory = new BoardCommunicationFactory();
        
        mockStrategies = new HashMap<>();
        BoardCommunicationStrategy restStrategy = mock(BoardCommunicationStrategy.class);
        BoardCommunicationStrategy websocketStrategy = mock(BoardCommunicationStrategy.class);
        
        mockStrategies.put("rest", restStrategy);
        mockStrategies.put("websocket", websocketStrategy);
        
        ReflectionTestUtils.setField(factory, "environment", environment);
        ReflectionTestUtils.setField(factory, "strategies", mockStrategies);
    }

    @Test
    void getStrategy_RestModeConfigured_ShouldReturnRestStrategy() {
        when(environment.getProperty("board.communication.mode", "rest")).thenReturn("rest");
        
        BoardCommunicationStrategy strategy = factory.getStrategy();
        
        assertNotNull(strategy);
        assertEquals(mockStrategies.get("rest"), strategy);
    }

    @Test
    void getStrategy_WebSocketModeConfigured_ShouldReturnWebSocketStrategy() {
        when(environment.getProperty("board.communication.mode", "rest")).thenReturn("websocket");
        
        BoardCommunicationStrategy strategy = factory.getStrategy();
        
        assertNotNull(strategy);
        assertEquals(mockStrategies.get("websocket"), strategy);
    }

    @Test
    void getStrategy_NoModeConfigured_ShouldReturnRestAsDefault() {
        when(environment.getProperty("board.communication.mode", "rest")).thenReturn("rest");
        
        BoardCommunicationStrategy strategy = factory.getStrategy();
        
        assertNotNull(strategy);
        assertEquals(mockStrategies.get("rest"), strategy);
    }

    @Test
    void getStrategy_InvalidModeConfigured_ShouldThrowException() {
        when(environment.getProperty("board.communication.mode", "rest")).thenReturn("invalid");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.getStrategy()
        );
        
        assertTrue(exception.getMessage().contains("Unknown communication mode: invalid"));
        assertTrue(exception.getMessage().contains("Available modes: [rest, websocket]"));
    }

    @Test
    void getStrategy_NonExistentModeConfigured_ShouldThrowException() {
        when(environment.getProperty("board.communication.mode", "rest")).thenReturn("nonexistent");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.getStrategy()
        );
        
        assertTrue(exception.getMessage().contains("Unknown communication mode: nonexistent"));
    }

    @Test
    void isWebSocketMode_WebSocketModeConfigured_ShouldReturnTrue() {
        when(environment.getProperty("board.communication.mode", "rest")).thenReturn("websocket");
        
        assertTrue(factory.isWebSocketMode());
    }

    @Test
    void isWebSocketMode_RestModeConfigured_ShouldReturnFalse() {
        when(environment.getProperty("board.communication.mode", "rest")).thenReturn("rest");
        
        assertFalse(factory.isWebSocketMode());
    }

    @Test
    void isWebSocketMode_NoModeConfigured_ShouldReturnFalse() {
        when(environment.getProperty("board.communication.mode", "rest")).thenReturn("rest");
        
        assertFalse(factory.isWebSocketMode());
    }

    @Test
    void getCurrentMode_RestModeConfigured_ShouldReturnRest() {
        when(environment.getProperty("board.communication.mode", "rest")).thenReturn("rest");
        
        assertEquals("rest", factory.getCurrentMode());
    }

    @Test
    void getCurrentMode_WebSocketModeConfigured_ShouldReturnWebSocket() {
        when(environment.getProperty("board.communication.mode", "rest")).thenReturn("websocket");
        
        assertEquals("websocket", factory.getCurrentMode());
    }

    @Test
    void getCurrentMode_NoModeConfigured_ShouldReturnRestAsDefault() {
        when(environment.getProperty("board.communication.mode", "rest")).thenReturn("rest");
        
        assertEquals("rest", factory.getCurrentMode());
    }
}
