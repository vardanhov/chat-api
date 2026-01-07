package com.spotware.chat_api.handler;

import com.spotware.chat_api.domain.Message;
import com.spotware.chat_api.dto.CreateMessageRequest;
import com.spotware.chat_api.dto.EditMessageRequest;
import com.spotware.chat_api.dto.ListMessageRequest;
import com.spotware.chat_api.dto.WsRequest;
import com.spotware.chat_api.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketHandlerTest {

    @Mock
    private ChatService service;

    @Mock
    private WebSocketSession session;

    @InjectMocks
    private ChatWebSocketHandler handler;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void shouldHandleCreateMessage() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();

        CreateMessageRequest payload =
            new CreateMessageRequest(userId, chatId, "hello");

        WsRequest request =
            new WsRequest("CREATE", payload);

        Message saved = new Message(
            UUID.randomUUID(),
            userId,
            chatId,
            1,
            0,
            "hello"
        );

        when(service.createMessage(userId, chatId, "hello"))
            .thenReturn(saved);

        TextMessage textMessage =
            new TextMessage(mapper.writeValueAsString(request));

        handler.handleTextMessage(session, textMessage);

        verify(service).createMessage(userId, chatId, "hello");

        ArgumentCaptor<TextMessage> response =
            ArgumentCaptor.forClass(TextMessage.class);

        verify(session).sendMessage(response.capture());

        Message result =
            mapper.readValue(response.getValue().getPayload(), Message.class);

        assertThat(result.getPayload()).isEqualTo("hello");
        assertThat(result.getMessageChatN()).isEqualTo(1);
    }

    @Test
    void shouldHandleEditMessage() throws Exception {
        UUID messageId = UUID.randomUUID();

        EditMessageRequest payload =
            new EditMessageRequest(messageId, 1, "updated");

        WsRequest request =
            new WsRequest("EDIT", payload);

        Message updated = new Message(
            messageId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            1,
            "updated"
        );

        when(service.editMessage(messageId, 1, "updated"))
            .thenReturn(updated);

        TextMessage textMessage =
            new TextMessage(mapper.writeValueAsString(request));

        handler.handleTextMessage(session, textMessage);

        verify(service).editMessage(messageId, 1, "updated");
        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void shouldHandleListMessages() throws Exception {
        UUID chatId = UUID.randomUUID();

        ListMessageRequest payload =
            new ListMessageRequest(chatId, 0, 2);

        WsRequest request =
            new WsRequest("LIST", payload);

        Message m1 =
            new Message(UUID.randomUUID(), UUID.randomUUID(), chatId, 1, 0, "a");
        Message m2 =
            new Message(UUID.randomUUID(), UUID.randomUUID(), chatId, 2, 0, "b");

        Page<Message> page =
            new PageImpl<>(List.of(m1, m2), PageRequest.of(0, 2), 2);

        when(service.list(chatId, 0, 2))
            .thenReturn(page);

        TextMessage textMessage =
            new TextMessage(mapper.writeValueAsString(request));

        handler.handleTextMessage(session, textMessage);

        verify(service).list(chatId, 0, 2);

        ArgumentCaptor<TextMessage> response =
            ArgumentCaptor.forClass(TextMessage.class);

        verify(session).sendMessage(response.capture());

        assertThat(response.getValue().getPayload())
            .contains("\"content\"");
    }

    @Test
    void shouldFailOnUnknownType() {
        WsRequest request =
            new WsRequest("UNKNOWN", null);

        TextMessage textMessage =
            new TextMessage(
                assertDoesNotThrow(() -> mapper.writeValueAsString(request))
            );

        assertThatThrownBy(() ->
            handler.handleTextMessage(session, textMessage)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown type");

        verifyNoInteractions(service);
    }
}
