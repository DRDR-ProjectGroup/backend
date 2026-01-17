package com.dorandoran.domain.message.dto.response;

import com.dorandoran.domain.message.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private Long messageId;
    private String senderName;
    private String receiverName;
    private String content;
    private String createdAt;

    public static MessageResponse of(Message message) {
        return MessageResponse.builder()
                .messageId(message.getId())
                .senderName(message.getSender().getNickname())
                .receiverName(message.getReceiver().getNickname())
                .content(message.getContent())
                .createdAt(message.getCreatedAt().toString())
                .build();
    }
}
