package com.dorandoran.domain.message.dto.response;

import com.dorandoran.domain.member.dto.response.AuthorResponse;
import com.dorandoran.domain.message.entity.Message;
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
    private Long messageId;
    private AuthorResponse senderName;
    private AuthorResponse receiverName;
    private String content;
    private LocalDateTime createdAt;

    public static MessageResponse of(Message message) {
        return MessageResponse.builder()
                .messageId(message.getId())
                .senderName(AuthorResponse.of(message.getSender()))
                .receiverName(AuthorResponse.of(message.getReceiver()))
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
