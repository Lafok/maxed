package com.maxed.app;

import com.maxed.chatservice.api.ChatService;
import com.maxed.chatservice.api.MessageResponse;
import com.maxed.chatservice.api.SendMessageRequest;
import com.maxed.userservice.api.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage/{chatId}")
    public void sendMessage(@DestinationVariable Long chatId, SendMessageRequest request, Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;



        var userDetails = (com.maxed.userservice.impl.User) token.getPrincipal();

        User currentUser = User.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getRole())
                .build();

        MessageResponse messageResponse = chatService.sendMessage(chatId, request, currentUser);
        messagingTemplate.convertAndSend("/topic/chats." + chatId, messageResponse);
    }
}
