package com.maxed.searchservice;

import com.maxed.app.security.PrincipalMapper;
import com.maxed.chatservice.api.ChatService;
import com.maxed.userservice.api.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final MessageElasticRepository elasticRepository;
    private final ChatService chatService;
    private final PrincipalMapper principalMapper;

    @GetMapping
    public List<MessageDoc> searchMessages(
            @RequestParam Long chatId,
            @RequestParam String query,
            Principal principal
    ) {
        User currentUser = principalMapper.toApiUser(principal);

        chatService.validateUserIsParticipant(chatId, currentUser.getId());
        return elasticRepository.findByChatIdAndContentContaining(chatId, query);
    }
}
