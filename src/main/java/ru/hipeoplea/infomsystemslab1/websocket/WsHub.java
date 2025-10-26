package ru.hipeoplea.infomsystemslab1.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WsHub extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WsHub.class);
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WS connected {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessions.remove(session);
        log.info("WS closed {}", session.getId());
    }

    public void broadcast(String json) {
        sessions.forEach(s -> {
            try { s.sendMessage(new TextMessage(json)); } catch (IOException ignored) {}
        });
    }
}

