package arsw.java.Live_Board.strategy;

import arsw.java.Live_Board.Services.BoardService;
import arsw.java.Live_Board.model.StrokeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Estrategia de comunicación usando WebSockets.
 * Envía mensajes a través de sesiones WebSocket conectadas.
 */
@Component("websocket")
public class WebSocketBoardStrategy implements BoardCommunicationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketBoardStrategy.class);

    @Autowired
    private BoardService boardService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void sendStroke(StrokeDto stroke) {
        try {
            boardService.addStroke(stroke);
            
            if (sessions.isEmpty()) {
                logger.warn("No active WebSocket sessions for stroke broadcast");
                return;
            }
            
            broadcastMessage("stroke", stroke);
            
        } catch (Exception e) {
            logger.error("Error sending stroke via WebSocket", e);
            throw new RuntimeException("Failed to send stroke", e);
        }
    }

    @Override
    public void sendClear() {
        try {
            logger.info("Clearing board, active sessions: {}", sessions.size());
            boardService.clear();
            
            if (sessions.isEmpty()) {
                logger.warn("No active WebSocket sessions for clear broadcast");
                return;
            }
            
            logger.info("Broadcasting clear message to {} sessions", sessions.size());
            try {
                broadcastMessage("clear", null);
                logger.info("Clear broadcast completed");
            } catch (Exception broadcastError) {
                logger.error("Broadcast failed but board was cleared", broadcastError);
                // No lanzar excepción, el tablero ya fue limpiado
            }
            
        } catch (Exception e) {
            logger.error("Error clearing board", e);
            throw new RuntimeException("Failed to clear board", e);
        }
    }

    @Override
    public Map<String, Object> sendHeartbeat(String userId) {
        try {
            Map<String, Object> response = boardService.updateUserActivity(userId);
            
            WebSocketSession session = sessions.get(userId);
            if (session != null && session.isOpen()) {
                sendMessage(session, "heartbeat", response);
            } else {
                logger.debug("No active WebSocket session found for user: {}", userId);
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error sending heartbeat via WebSocket", e);
            throw new RuntimeException("Failed to send heartbeat", e);
        }
    }

    @Override
    public Object getBoard() {
        return boardService.getBoard();
    }

    @Override
    public Map<String, Object> getBoardInfo() {
        return boardService.getBoardInfo();
    }

    @Override
    public Map<String, Object> getChangesSince(Long since) {
        Map<String, Object> changes = boardService.getChangesSince(since);
        
        if (!sessions.isEmpty()) {
            broadcastMessage("changes", changes);
        }
        
        return changes;
    }

    public void addSession(String userId, WebSocketSession session) {
        sessions.put(userId, session);
        logger.info("WebSocket session added for user: {} - Total sessions: {}", userId, sessions.size());
    }

    public void removeSession(String userId) {
        sessions.remove(userId);
        logger.info("WebSocket session removed for user: {} - Total sessions: {}", userId, sessions.size());
    }
    
    public int getSessionCount() {
        return sessions.size();
    }

    public void broadcastMessage(String type, Object data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", type);
            message.put("data", data);
            String jsonMessage = objectMapper.writeValueAsString(message);
            logger.info("🔥 BROADCASTING: {} to {} sessions", jsonMessage, sessions.size());
            
            int[] sentCount = {0};
            int[] removedCount = {0};
            
            sessions.values().removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(jsonMessage));
                        sentCount[0]++;
                        return false;
                    } else {
                        logger.debug("Removing closed session: {}", session.getId());
                        removedCount[0]++;
                        return true;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to send message to session {}, removing: {}", session.getId(), e.getMessage());
                    removedCount[0]++;
                    return true;
                }
            });
            
            logger.info("🔥 BROADCAST COMPLETED: sent to {} sessions, removed {} sessions", sentCount[0], removedCount[0]);
            
        } catch (Exception e) {
            logger.error("Error creating broadcast message", e);
            throw new RuntimeException("Failed to create broadcast message", e);
        }
    }

    private void sendMessage(WebSocketSession session, String type, Object data) {
        try {
            Map<String, Object> message = Map.of("type", type, "data", data);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (Exception e) {
            logger.error("Failed to send message to session", e);
        }
    }
}
