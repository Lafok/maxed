package com.maxed.app;

import com.maxed.chatservice.api.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/direct")
    @Operation(summary = "Create a new direct chat", description = "Creates a new one-on-one chat with another user. If a chat already exists, it returns the existing one.")
    public ResponseEntity<ChatResponse> createDirectChat(@RequestBody CreateDirectChatRequest request) {
        ChatResponse chatResponse = chatService.createDirectChat(request);
        return new ResponseEntity<>(chatResponse, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all chats for the current user", description = "Fetches a list of all chats (both direct and group) that the currently authenticated user is a part of.")
    public ResponseEntity<List<ChatResponse>> getChatsForCurrentUser() {
        return ResponseEntity.ok(chatService.getChatsForCurrentUser());
    }

    @GetMapping("/{chatId}")
    @Operation(summary = "Get a specific chat by its ID", description = "Retrieves the details of a specific chat, provided the current user is a participant.")
    public ResponseEntity<ChatResponse> getChatById(@PathVariable Long chatId) {
        return ResponseEntity.ok(chatService.getChatById(chatId));
    }

    @PostMapping("/{chatId}/messages")
    @Operation(summary = "Send a message to a chat", description = "Sends a new message to the specified chat. The authenticated user must be a participant of the chat.")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable Long chatId, @RequestBody SendMessageRequest request, Principal principal) {
        MessageResponse messageResponse = chatService.sendMessage(chatId, request, principal);
        return new ResponseEntity<>(messageResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{chatId}/messages")
    @Operation(summary = "Get message history for a chat", description = "Fetches a paginated list of messages for the specified chat. The authenticated user must be a participant.")
    public ResponseEntity<Page<MessageResponse>> getMessagesForChat(@PathVariable Long chatId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(chatService.getMessagesForChat(chatId, pageable));
    }
}
