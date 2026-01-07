package com.spotware.chat_api.dto;

import java.util.UUID;

public record CreateMessageRequest(
    UUID userId,
    UUID chatId,
    String payload
) {}