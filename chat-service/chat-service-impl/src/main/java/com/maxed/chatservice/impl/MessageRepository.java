package com.maxed.chatservice.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatIdOrderByTimestampDesc(Long chatId, Pageable pageable);
    Optional<Message> findFirstByChatIdOrderByTimestampDesc(Long chatId);

    @Modifying
    @Query("UPDATE Message m SET m.status = 'READ' WHERE m.chat.id = :chatId AND m.authorId != :currentUserId AND m.status = 'SENT'")
    void markMessagesAsRead(@Param("chatId") Long chatId, @Param("currentUserId") Long currentUserId);
}
