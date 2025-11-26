package ru.hipeoplea.is.lab1.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WsHub extends TextWebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(WsHub.class);
    private final Set<WebSocketSession> sessions =
            ConcurrentHashMap.newKeySet();

    /**
     * Registers a new WebSocket session; subclasses should invoke super if
     * overriding to keep the session registry in sync.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        LOG.info("WS connected {}", session.getId());
    }

    /**
     * Removes a closed WebSocket session; subclasses should invoke super if
     * overriding to keep the session registry in sync.
     */
    @Override
    public void afterConnectionClosed(
            WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        LOG.info("WS closed {}", session.getId());
    }

    /**
     * Sends the provided JSON payload to all connected sessions.
     */
    public void broadcast(String json) {
        sessions.forEach(
                s -> {
                    try {
                        s.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        LOG.error(
                                "Failed to send WS message to {}",
                                s.getId(),
                                e);
                    }
                });
    }
}
