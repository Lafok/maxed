package com.maxed.app;

import com.maxed.chatservice.api.ChatService;
import com.maxed.chatservice.api.MessageResponse;
import com.maxed.chatservice.api.SendMessageRequest;
import com.maxed.userservice.api.User; // Используем API-версию User
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage/{chatId}")
    public void sendMessage(@DestinationVariable Long chatId, SendMessageRequest request, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof com.maxed.userservice.impl.User implUser)) {
            throw new IllegalStateException("Authenticated principal is not an instance of com.maxed.userservice.impl.User.");
        }
        User currentUser = User.builder()
                .id(implUser.getId())
                .username(implUser.getUsername())
                .email(implUser.getEmail())
                .role(implUser.getRole())
                .build();

        MessageResponse messageResponse = chatService.sendMessage(chatId, request, currentUser);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, messageResponse);
    }
}
