package com.spotware.chat_api.dto;

import java.util.UUID;

public record ListMessageRequest(
    UUID chatId,
    int page,
    int size
) {}