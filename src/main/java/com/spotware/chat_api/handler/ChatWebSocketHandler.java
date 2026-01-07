package com.spotware.chat_api.handler;


import com.spotware.chat_api.dto.CreateMessageRequest;
import com.spotware.chat_api.dto.EditMessageRequest;
import com.spotware.chat_api.dto.ListMessageRequest;
import com.spotware.chat_api.dto.WsRequest;
import com.spotware.chat_api.service.ChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatWebSocketHandler(ChatService service) {
        this.service = service;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
        throws Exception {

        WsRequest req = mapper.readValue(message.getPayload(), WsRequest.class);

        Object result = switch (req.type()) {
            case "CREATE" -> {
                CreateMessageRequest r =
                    mapper.convertValue(req.payload(), CreateMessageRequest.class);
                yield service.createMessage(r.userId(), r.chatId(), r.payload());
            }
            case "EDIT" -> {
                EditMessageRequest r =
                    mapper.convertValue(req.payload(), EditMessageRequest.class);
                yield service.editMessage(r.messageId(), r.version(), r.payload());
            }
            case "LIST" -> {
                ListMessageRequest r =
                    mapper.convertValue(req.payload(), ListMessageRequest.class);
                yield service.list(r.chatId(), r.page(), r.size());
            }
            default -> throw new IllegalArgumentException("Unknown type");
        };

        session.sendMessage(
            new TextMessage(mapper.writeValueAsString(result))
        );
    }
}