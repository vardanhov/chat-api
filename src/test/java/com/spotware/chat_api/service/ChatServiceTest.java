package com.spotware.chat_api.service;

import com.spotware.chat_api.domain.Message;
import com.spotware.chat_api.exception.ApiException;
import com.spotware.chat_api.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private MessageRepository repository;

    @InjectMocks
    private ChatService service;

    private UUID userId;
    private UUID chatId;
    private UUID messageId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        chatId = UUID.randomUUID();
        messageId = UUID.randomUUID();
    }

    @Test
    void createMessage_shouldIncrementMessageChatN() {
        when(repository.findLastMessageNumberForUpdate(chatId))
                .thenReturn(Optional.of(5));

        ArgumentCaptor<Message> captor =
                ArgumentCaptor.forClass(Message.class);

        when(repository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message result =
                service.createMessage(userId, chatId, "hello");

        verify(repository).save(captor.capture());

        Message saved = captor.getValue();
        assertThat(saved.getMessageChatN()).isEqualTo(6);
        assertThat(saved.getVersion()).isEqualTo(0);
        assertThat(saved.getPayload()).isEqualTo("hello");
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getChatId()).isEqualTo(chatId);

        assertThat(result).isSameAs(saved);
    }

    @Test
    void createMessage_shouldStartFromOneIfNoMessages() {

        when(repository.findLastMessageNumberForUpdate(chatId))
                .thenReturn(Optional.empty());

        when(repository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message result =
                service.createMessage(userId, chatId, "first");

        assertThat(result.getMessageChatN()).isEqualTo(1);
    }

    @Test
    void editMessage_shouldUpdatePayload_whenVersionMatches() {
        // given
        Message message = new Message(
                messageId,
                userId,
                chatId,
                1,
                2,
                "old"
        );

        when(repository.findById(messageId))
                .thenReturn(Optional.of(message));

        when(repository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message result =
                service.editMessage(messageId, 2, "new");

        assertThat(result.getPayload()).isEqualTo("new");
        verify(repository).save(message);
    }

    @Test
    void editMessage_shouldThrow_whenMessageNotFound() {

        when(repository.findById(messageId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.editMessage(messageId, 0, "x")
        )
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Message not found");
    }

    @Test
    void editMessage_shouldThrowOptimisticLock_whenVersionMismatch() {
        Message message = new Message(
                messageId,
                userId,
                chatId,
                1,
                1,
                "old"
        );

        when(repository.findById(messageId))
                .thenReturn(Optional.of(message));

        assertThatThrownBy(() ->
                service.editMessage(messageId, 99, "new")
        )
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Version mismatch");

        verify(repository, never()).save(any());
    }

    @Test
    void list_shouldReturnPagedMessagesOrderedByMessageChatN() {
        Message m1 =
                new Message(UUID.randomUUID(), userId, chatId, 1, 0, "a");
        Message m2 =
                new Message(UUID.randomUUID(), userId, chatId, 2, 0, "b");

        Page<Message> page = new PageImpl<>(
                List.of(m1, m2),
                PageRequest.of(0, 2),
                2
        );

        when(repository.findByChatIdOrderByMessageChatNAsc(
                eq(chatId),
                any(PageRequest.class)
        )).thenReturn(page);

        Page<Message> result =
                service.list(chatId, 0, 2);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Message::getMessageChatN)
                .containsExactly(1, 2);
    }
}
