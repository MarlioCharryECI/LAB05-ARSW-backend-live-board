package arsw.java.Live_Board.factory;

import arsw.java.Live_Board.strategy.BoardCommunicationStrategy;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Factory que selecciona la estrategia de comunicación del tablero basándose en la configuración de
 * la aplicación.
 */
@Component
public class BoardCommunicationFactory {

  @Autowired private Environment environment;

  @Autowired private Map<String, BoardCommunicationStrategy> strategies;

  /**
   * Obtiene la estrategia de comunicación configurada. Por defecto usa REST si no se especifica
   * nada.
   *
   * @return Estrategia de comunicación activa
   */
  public BoardCommunicationStrategy getStrategy() {
    String mode = environment.getProperty("board.communication.mode", "rest");

    BoardCommunicationStrategy strategy = strategies.get(mode);
    if (strategy == null) {
      throw new IllegalArgumentException(
          "Unknown communication mode: " + mode + ". Available modes: " + strategies.keySet());
    }

    return strategy;
  }

  /**
   * Verifica si el modo actual es WebSocket.
   *
   * @return true si está configurado para usar WebSockets
   */
  public boolean isWebSocketMode() {
    String mode = environment.getProperty("board.communication.mode", "rest");
    return "websocket".equalsIgnoreCase(mode);
  }

  /**
   * Obtiene el modo de comunicación actual.
   *
   * @return Modo configurado (rest o websocket)
   */
  public String getCurrentMode() {
    return environment.getProperty("board.communication.mode", "rest");
  }
}
