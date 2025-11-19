package com.maxed.app.config;

import com.maxed.app.config.websocket.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Регистрируем конечную точку /ws, разрешая подключения от фронтенда
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:5173");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Включаем простой брокер сообщений для отправки клиентам по префиксам /topic и /user
        registry.enableSimpleBroker("/topic", "/user");
        // Устанавливаем префикс для сообщений, адресованных @MessageMapping методам в контроллерах
        registry.setApplicationDestinationPrefixes("/app");
        // Устанавливаем префикс для сообщений, адресованных конкретным пользователям
        registry.setUserDestinationPrefix("/user");
    }
}
