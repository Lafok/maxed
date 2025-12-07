package com.maxed.app;

import com.maxed.app.security.PrincipalMapper;
import com.maxed.chatservice.api.ChatService;
import com.maxed.chatservice.api.MessageResponse;
import com.maxed.chatservice.api.SendMessageRequest;
import com.maxed.chatservice.api.TypingEvent;
import com.maxed.userservice.api.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PrincipalMapper principalMapper;

    @MessageMapping("/chat.sendMessage/{chatId}")
    public void sendMessage(@DestinationVariable Long chatId, @Payload SendMessageRequest request, Principal principal) {
        User currentUser = principalMapper.toApiUser(principal);

        MessageResponse messageResponse = chatService.sendMessage(chatId, request, currentUser);
        messagingTemplate.convertAndSend("/topic/chats." + chatId, messageResponse);
    }

    @MessageMapping("/chat.typing/{chatId}")
    public void handleTyping(@DestinationVariable Long chatId, @Payload TypingEvent event, Principal principal) {
        User currentUser = principalMapper.toApiUser(principal);

        chatService.validateUserIsParticipant(chatId, currentUser.getId());

        TypingEvent broadcastEvent = new TypingEvent(
                chatId,
                currentUser.getUsername(),
                event.isTyping()
        );

        messagingTemplate.convertAndSend("/topic/chats." + chatId + ".typing", broadcastEvent);
    }
}
