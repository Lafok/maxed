package com.maxed.searchservice;

import com.maxed.chatservice.api.MessageType;
import com.maxed.common.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageIndexer {
    private final MessageElasticRepository elasticRepository;

    @KafkaListener(topics = "chat.messages", groupId = "search-service-group")
    public void listen(MessageCreatedEvent event) {
        if (event.type() == MessageType.TEXT) {
            MessageDoc doc = new MessageDoc(
                    event.id(),
                    event.content(),
                    event.chatId(),
                    event.authorId(),
                    event.timestamp()
            );
            elasticRepository.save(doc);
        }
    }
}
