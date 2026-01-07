package com.spotware.chat_api.dto;

import java.util.UUID;

public record EditMessageRequest(
    UUID messageId,
    Integer version,
    String payload
) {}