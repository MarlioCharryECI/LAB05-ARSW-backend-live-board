package arsw.java.Live_Board.strategy;

import arsw.java.Live_Board.model.StrokeDto;
import java.util.Map;

/**
 * Interfaz que define la estrategia de comunicación del tablero. Permite cambiar entre REST y
 * WebSockets sin modificar el controlador.
 */
public interface BoardCommunicationStrategy {

  /**
   * Envía un trazo al tablero.
   *
   * @param stroke Trazo a enviar
   */
  void sendStroke(StrokeDto stroke);

  /** Limpia el tablero. */
  void sendClear();

  /**
   * Envía un heartbeat para mantener la sesión activa.
   *
   * @param userId ID del usuario
   * @return Información del usuario después del heartbeat
   */
  Map<String, Object> sendHeartbeat(String userId);

  /**
   * Obtiene el estado actual del tablero.
   *
   * @return Lista de trazos actuales
   */
  Object getBoard();

  /**
   * Obtiene información del tablero para sincronización.
   *
   * @return Información del estado actual
   */
  Map<String, Object> getBoardInfo();

  /**
   * Obtiene cambios desde un timestamp específico.
   *
   * @param since Timestamp desde el cual buscar cambios
   * @return Cambios y metadata
   */
  Map<String, Object> getChangesSince(Long since);
}
