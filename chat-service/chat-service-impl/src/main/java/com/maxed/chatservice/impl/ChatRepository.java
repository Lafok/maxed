package com.maxed.chatservice.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("SELECT c FROM Chat c JOIN c.participants p1 JOIN c.participants p2 WHERE c.type = 'DIRECT' AND p1.id = :userId1 AND p2.id = :userId2")
    Optional<Chat> findDirectChatBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT c FROM Chat c JOIN FETCH c.participants p WHERE p.id = :userId")
    List<Chat> findChatsByParticipantId(@Param("userId") Long userId);

    @Query("SELECT c, m FROM Chat c " +
           "JOIN FETCH c.participants p " + // Добавлено FETCH для участников
           "LEFT JOIN Message m ON m.chat = c AND m.timestamp = (SELECT MAX(m2.timestamp) FROM Message m2 WHERE m2.chat = c) " +
           "WHERE p.id = :userId")
    List<Object[]> findChatsAndLatestMessageByParticipantId(@Param("userId") Long userId);

    @Query("SELECT c FROM Chat c JOIN FETCH c.participants WHERE c.id = :id")
    Optional<Chat> findByIdWithParticipants(@Param("id") Long id);
}
