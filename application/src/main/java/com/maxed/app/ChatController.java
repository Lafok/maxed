package com.maxed.app;

import com.maxed.chatservice.api.ChatResponse;
import com.maxed.chatservice.api.ChatService;
import com.maxed.chatservice.api.CreateDirectChatRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/direct")
    public ResponseEntity<ChatResponse> createDirectChat(@RequestBody CreateDirectChatRequest request) {
        ChatResponse chatResponse = chatService.createDirectChat(request);
        return new ResponseEntity<>(chatResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ChatResponse>> getChatsForCurrentUser() {
        return ResponseEntity.ok(chatService.getChatsForCurrentUser());
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponse> getChatById(@PathVariable Long chatId) {
        return ResponseEntity.ok(chatService.getChatById(chatId));
    }
}
