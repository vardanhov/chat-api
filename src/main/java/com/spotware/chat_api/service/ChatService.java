package com.spotware.chat_api.service;


import com.spotware.chat_api.domain.Message;
import com.spotware.chat_api.exception.ApiException;
import com.spotware.chat_api.repository.MessageRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChatService {

    private final MessageRepository repository;

    public ChatService(MessageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Message createMessage(UUID userId, UUID chatId, String payload) {

        int last =
            repository.findLastMessageNumberForUpdate(chatId)
                .orElse(0);

        Message message = new Message(
            UUID.randomUUID(),
            userId,
            chatId,
            last + 1,
            0,
            payload
        );

        return repository.save(message);
    }

    @Transactional
    public Message editMessage(UUID messageId, Integer version, String payload) {

        Message message = repository.findById(messageId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "Message not found"));

        if (!message.getVersion().equals(version)) {
            throw new ApiException("OPTIMISTIC_LOCK", "Version mismatch");
        }

        message.setPayload(payload);
        message.setVersion(message.getVersion() + 1);
        return repository.save(message);
    }

    @Transactional(readOnly = true)
    public Page<Message> list(UUID chatId, int page, int size) {
        return repository.findByChatIdOrderByMessageChatNAsc(
            chatId,
            PageRequest.of(page, size)
        );
    }
}