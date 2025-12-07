package com.maxed.searchservice;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "messages")
public class MessageDoc {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Long)
    private Long chatId;

    @Field(type = FieldType.Long)
    private Long authorId;

    @Field(type = FieldType.Date, format = {
            DateFormat.date_hour_minute_second_millis,
            DateFormat.date_hour_minute_second
    })
    private LocalDateTime timestamp;

    public MessageDoc(String id, String content, Long chatId, Long authorId, LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.chatId = chatId;
        this.authorId = authorId;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getContent() { return content; }
    public Long getChatId() { return chatId; }
    public Long getAuthorId() { return authorId; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
