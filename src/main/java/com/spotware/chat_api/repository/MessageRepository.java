package com.spotware.chat_api.repository;


import com.spotware.chat_api.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT m.messageChatN
        FROM Message m
        WHERE m.chatId = :chatId
        ORDER BY m.messageChatN DESC
        """)
    Optional<Integer> findLastMessageNumberForUpdate(UUID chatId);

    Page<Message> findByChatIdOrderByMessageChatNAsc(UUID chatId, Pageable pageable);
}