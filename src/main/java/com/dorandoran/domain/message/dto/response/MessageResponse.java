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
    private AuthorResponse sender;
    private AuthorResponse receiver;
    private String content;
    private LocalDateTime createdAt;

    public static MessageResponse of(Message message) {
        return MessageResponse.builder()
                .messageId(message.getId())
                .sender(AuthorResponse.of(message.getSender()))
                .receiver(AuthorResponse.of(message.getReceiver()))
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
