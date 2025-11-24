package ru.hipeoplea.is.lab1.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final WsHub wsHub;

    public WebSocketConfig(WsHub wsHub) {
        this.wsHub = wsHub;
    }

    /**
     * Registers WebSocket handlers; override with care to keep the hub
     * mapping.
     */
    @Override
    public void registerWebSocketHandlers(
            org.springframework.web.socket.config.annotation.
                    WebSocketHandlerRegistry registry) {
        registry.addHandler(wsHub, "/ws").setAllowedOrigins("*");
    }
}
