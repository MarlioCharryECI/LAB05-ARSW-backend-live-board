package arsw.java.Live_Board.config;

import arsw.java.Live_Board.strategy.WebSocketBoardStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Map;

/**
 * Configuración WebSocket para comunicación en tiempo real.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    private final WebSocketBoardStrategy webSocketStrategy;

    @Bean
    public WebSocketBoardStrategy webSocketBoardStrategy() {
        logger.info("🔥 CREATING WEBSOCKET BOARD STRATEGY BEAN");
        return new WebSocketBoardStrategy();
    }

    @Autowired
    public WebSocketConfig(WebSocketBoardStrategy webSocketStrategy) {
        logger.info("🔥 WEBSOCKET CONFIG CONSTRUCTOR CALLED");
        logger.info("🔥 STRATEGY INJECTED: {}", webSocketStrategy.getClass().getSimpleName());
        this.webSocketStrategy = webSocketStrategy;
        logger.info("🔥 WEBSOCKET CONFIG INITIALIZED");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        logger.info("🔥 REGISTERING WEBSOCKET HANDLER at /ws");
        BoardWebSocketHandler handler = new BoardWebSocketHandler(webSocketStrategy);
        logger.info("🔥 HANDLER CREATED: {}", handler.getClass().getSimpleName());
        
        // Registrar el handler con múltiples configuraciones para asegurar que funcione
        registry.addHandler(handler, "/ws")
                .setAllowedOrigins("*")
                .withSockJS();
        
        registry.addHandler(handler, "/ws")
                .setAllowedOrigins("*");
        
        logger.info("🔥 WEBSOCKET HANDLER REGISTERED SUCCESSFULLY");
    }

    /**
     * Handler WebSocket para manejar conexiones de clientes del tablero.
     */
    public static class BoardWebSocketHandler extends TextWebSocketHandler {
        
        private static final Logger logger = LoggerFactory.getLogger(BoardWebSocketHandler.class);
        private final WebSocketBoardStrategy webSocketStrategy;

        public BoardWebSocketHandler(WebSocketBoardStrategy webSocketStrategy) {
            logger.info("🔥 BOARD WEBSOCKET HANDLER CONSTRUCTOR CALLED");
            this.webSocketStrategy = webSocketStrategy;
            this.objectMapper = new ObjectMapper();
            logger.info("🔥 BOARD WEBSOCKET HANDLER INITIALIZED");
        }
        
        private final ObjectMapper objectMapper;

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            logger.info("🔥 CONNECTION ESTABLISHED - Session: {}", session.getId());
            String userId = extractUserId(session);
            if (userId != null) {
                webSocketStrategy.addSession(userId, session);
                logger.info("🔥 WebSocket connection established for user: {} - Session: {} - Total sessions: {}", 
                           userId, session.getId(), webSocketStrategy.getSessionCount());
            } else {
                logger.warn("🔥 WebSocket connection without userId, closing session: {}", session.getId());
                session.close();
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            String userId = extractUserId(session);
            if (userId != null) {
                webSocketStrategy.removeSession(userId);
                logger.info("WebSocket connection closed for user: {} - Session: {} - Status: {}", 
                           userId, session.getId(), status);
            }
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            logger.info("🔥🔥🔥 handleTextMessage CALLED - Session: {}", session.getId());
            
            String userId = extractUserId(session);
            if (userId == null) {
                logger.warn("🔥 Message from session without userId, closing: {}", session.getId());
                session.close();
                return;
            }

            try {
                String payload = message.getPayload();
                logger.info("🔥🔥🔥 RAW MESSAGE RECEIVED from user {}: {}", userId, payload);
                
                // Parsear mensaje JSON para hacer broadcast
                try {
                    Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
                    String messageType = (String) messageData.get("type");
                    
                    logger.info("🔥🔥🔥 PARSED MESSAGE - type: {}, user: {}", messageType, userId);
                    
                    if ("clear".equals(messageType)) {
                        logger.info("🔥🔥🔥 BROADCASTING CLEAR MESSAGE from user: {}", userId);
                        logger.info("🔥🔥🔥 ACTIVE SESSIONS: {}", webSocketStrategy.getSessionCount());
                        
                        try {
                            // Hacer broadcast del mensaje clear a todos los demás clientes
                            webSocketStrategy.broadcastMessage("clear", null);
                            logger.info("🔥🔥🔥 CLEAR BROADCAST COMPLETED");
                        } catch (Exception e) {
                            logger.error("🔥🔥🔥 BROADCAST ERROR: {}", e.getMessage(), e);
                        }
                    } else {
                        logger.info("🔥🔥🔥 IGNORED MESSAGE TYPE: {}", messageType);
                    }
                } catch (Exception parseError) {
                    logger.error("🔥🔥🔥 PARSE ERROR: {}", parseError.getMessage(), parseError);
                }

            } catch (Exception e) {
                logger.error("🔥🔥🔥 HANDLER ERROR from user {}: {}", userId, e.getMessage(), e);
                session.close();
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
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
