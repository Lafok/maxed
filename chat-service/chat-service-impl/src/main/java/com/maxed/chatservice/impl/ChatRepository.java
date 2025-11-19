package com.maxed.chatservice.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {


    @Query("SELECT c FROM Chat c WHERE c.type = 'DIRECT' AND :userId1 MEMBER OF c.participantIds AND :userId2 MEMBER OF c.participantIds")
    Optional<Chat> findDirectChatBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT c FROM Chat c WHERE :userId MEMBER OF c.participantIds")
    List<Chat> findChatsByParticipantId(@Param("userId") Long userId);
}
