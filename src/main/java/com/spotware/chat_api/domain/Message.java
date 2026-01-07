package com.spotware.chat_api.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
    name = "message",
    uniqueConstraints = @UniqueConstraint(
        name = "message_ux1",
        columnNames = {"chat_id", "message_chat_n", "version"}
    )
)
public class Message {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID chatId;

    @Column(name = "message_chat_n", nullable = false)
    private Integer messageChatN;

    @Version
    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private String payload;

    protected Message() {}

    public Message(UUID id, UUID userId, UUID chatId,
                   Integer messageChatN, Integer version, String payload) {
        this.id = id;
        this.userId = userId;
        this.chatId = chatId;
        this.messageChatN = messageChatN;
        this.version = version;
        this.payload = payload;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getChatId() { return chatId; }
    public Integer getMessageChatN() { return messageChatN; }
    public Integer getVersion() { return version; }
    public String getPayload() { return payload; }

    public void setPayload(String payload) {
        this.payload = payload;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }
}