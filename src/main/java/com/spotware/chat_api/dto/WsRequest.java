package com.spotware.chat_api.dto;

public record WsRequest(
    String type,
    Object payload
) {}