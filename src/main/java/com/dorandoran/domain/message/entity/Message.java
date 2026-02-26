package com.dorandoran.domain.message.entity;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member receiver;

    @Column(nullable = false)
    private String content;

    private LocalDateTime senderDeletedAt;

    private LocalDateTime receiverDeletedAt;

    @Builder
    public Message(Member sender, Member receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public static Message createMessage(Member sender, Member receiver, String content) {
        return Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();
    }

    public void setSenderDeleted() {
        this.senderDeletedAt = LocalDateTime.now();
    }

    public void setReceiverDeleted() {
        this.receiverDeletedAt = LocalDateTime.now();
    }
}
