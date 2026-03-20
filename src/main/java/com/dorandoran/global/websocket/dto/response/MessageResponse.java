package com.dorandoran.global.websocket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private String memberId;
    private String nickname;
    private String message;
    private LocalDateTime timestamp;

    public static MessageResponse of(String memberId, String nickname, String message, LocalDateTime timestamp) {
        return MessageResponse.builder()
                .memberId(memberId)
                .nickname(nickname)
                .message(message)
                .timestamp(timestamp)
                .build();
    }
}
