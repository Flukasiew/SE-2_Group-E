package com.pw.server.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
@EqualsAndHashCode
public class PlayerMessage {
    private final String playerGuid;
    private final String message;
}
