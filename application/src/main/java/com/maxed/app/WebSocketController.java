package com.maxed.app;

import com.maxed.chatservice.api.ChatService;
import com.maxed.chatservice.api.MessageResponse;
import com.maxed.chatservice.api.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage/{chatId}")
    public void sendMessage(@DestinationVariable String chatId, SendMessageRequest request, Principal principal) {
        // The Principal object is populated by Spring Security from the WebSocket session
        // We can trust it to be the authenticated user
        MessageResponse messageResponse = chatService.sendMessage(Long.valueOf(chatId), request);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, messageResponse);
    }
}
