package com.zhituxing.chat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.*;
@Configuration @EnableWebSocket @EnableScheduling @RequiredArgsConstructor
public class ChatWebSocketConfig implements WebSocketConfigurer {
 private final ChatSocket socket;
 @Value("${zhituxing.chat.allowed-origins:http://localhost:23306,http://127.0.0.1:23306}") private String[] origins;
 @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){registry.addHandler(socket,"/ws/chat").setAllowedOrigins(origins);}
}
