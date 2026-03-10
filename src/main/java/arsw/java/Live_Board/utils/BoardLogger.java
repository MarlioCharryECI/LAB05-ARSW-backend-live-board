package arsw.java.Live_Board.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilidad centralizada para logging del BoardService. Proporciona métodos estáticos para todos los
 * eventos del tablero.
 */
public class BoardLogger {

  private static final Logger logger = LoggerFactory.getLogger(BoardLogger.class);

  public static void logUserJoining(String userId) {
    logger.info("Usuario intentando unirse: {}", userId);
  }

  public static void logNewColorAssigned(String userId, String color) {
    logger.info("Nuevo color asignado al usuario {}: {}", userId, color);
  }

  public static void logUserExists(String userId, String color) {
    logger.debug("Usuario {} ya existe con color: {}", userId, color);
  }

  public static void logUserActivityUpdated(String userId) {
    logger.debug("Actividad actualizada para usuario: {}", userId);
  }

  public static void logUserRemoved(String userId) {
    logger.debug("Usuario {} removido del tablero", userId);
  }

  public static void logInactiveUsersCleaned(int count) {
    logger.info("Limpiados {} usuarios inactivos", count);
  }

  public static void logBoardStrokesCount(int count) {
    logger.debug("Obteniendo {} trazos del tablero", count);
  }

  public static void logBoardStrokesTotal(int count) {
    logger.debug("Total de trazos en el tablero: {}", count);
  }

  public static void logBoardCleared(int count) {
    logger.info("Tablero limpiado. Se eliminaron {} trazos", count);
  }

  public static void logStrokeAdded(String userId, int pointsCount) {
    logger.debug("Agregando trazo del usuario: {} con {} puntos", userId, pointsCount);
  }
}
