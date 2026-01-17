package com.dorandoran.factory;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.message.entity.Message;
import com.dorandoran.domain.message.repository.MessageRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class MessageFactory {

    private final EntityManager em;
    private final MessageRepository messageRepository;

    public Message saveAndCreateMessage(Member sender, Member receiver, String content) {
        Message message = Message.createMessage(sender, receiver, content);
        
        Message savedMessage = messageRepository.save(message);

        flushAndClear();

        return savedMessage;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
