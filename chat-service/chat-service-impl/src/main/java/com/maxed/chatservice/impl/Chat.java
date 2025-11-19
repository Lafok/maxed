package com.maxed.chatservice.impl;

import com.maxed.chatservice.api.ChatType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ChatType type;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chat_participants", joinColumns = @JoinColumn(name = "chat_id"))
    @Column(name = "user_id")
    private Set<Long> participantIds = new HashSet<>();
}
