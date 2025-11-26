package com.maxed.app.config.websocket;

import com.maxed.chatservice.impl.presence.PresenceService;
import com.maxed.userservice.impl.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof UsernamePasswordAuthenticationToken token) {
            var userDetails = (User) token.getPrincipal();
            presenceService.setUserOnline(userDetails.getId());
            log.info("User connected: {}", userDetails.getUsername());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();

        if (principal instanceof UsernamePasswordAuthenticationToken token) {
            var userDetails = (User) token.getPrincipal();
            presenceService.setUserOffline(userDetails.getId());
            log.info("User disconnected: {}", userDetails.getUsername());
        }
    }
}
