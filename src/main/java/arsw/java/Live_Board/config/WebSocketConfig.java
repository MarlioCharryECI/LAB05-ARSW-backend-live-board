package arsw.java.Live_Board.config;

import arsw.java.Live_Board.strategy.WebSocketBoardStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
  private final WebSocketBoardStrategy webSocketStrategy;

  @Bean
  public WebSocketBoardStrategy webSocketBoardStrategy() {
    return new WebSocketBoardStrategy();
  }

  @Autowired
  public WebSocketConfig(WebSocketBoardStrategy webSocketStrategy) {
    this.webSocketStrategy = webSocketStrategy;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(new BoardWebSocketHandler(webSocketStrategy), "/ws").setAllowedOrigins("*");
  }

  public static class BoardWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(BoardWebSocketHandler.class);
    private final WebSocketBoardStrategy webSocketStrategy;
    private final ObjectMapper objectMapper;

    public BoardWebSocketHandler(WebSocketBoardStrategy webSocketStrategy) {
      this.webSocketStrategy = webSocketStrategy;
      this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
      String userId = extractUserId(session);
      if (userId != null) {
        webSocketStrategy.addSession(userId, session);
        logger.info(
            "WebSocket connection established for user: {} - Session: {} - Total sessions: {}",
            userId,
            session.getId(),
            webSocketStrategy.getSessionCount());
      } else {
        logger.warn("WebSocket connection without userId, closing session: {}", session.getId());
        session.close();
      }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
        throws Exception {
      String userId = extractUserId(session);
      if (userId != null) {
        webSocketStrategy.removeSession(userId);
        logger.info(
            "WebSocket connection closed for user: {} - Session: {} - Status: {}",
            userId,
            session.getId(),
            status);
      }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
        throws Exception {
      String userId = extractUserId(session);
      if (userId == null) {
        logger.warn("Message from session without userId, closing: {}", session.getId());
        session.close();
        return;
      }

      try {
        String payload = message.getPayload();
        Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
        String messageType = (String) messageData.get("type");

        if ("clear".equals(messageType)) {
          webSocketStrategy.broadcastMessage("clear", null);
        }

      } catch (Exception e) {
        logger.error("Error handling WebSocket message from user {}: {}", userId, e.getMessage());
        session.close();
      }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception)
        throws Exception {
      logger.error("Transport error for session {}: {}", session.getId(), exception.getMessage());
      String userId = extractUserId(session);
      if (userId != null) {
        webSocketStrategy.removeSession(userId);
      }
    }

    private String extractUserId(WebSocketSession session) {
      String query = session.getUri().getQuery();
      if (query != null && query.contains("userId=")) {
        String[] params = query.split("&");
        for (String param : params) {
          if (param.startsWith("userId=")) {
            return param.substring(7);
          }
        }
      }
      return null;
    }
  }
}
