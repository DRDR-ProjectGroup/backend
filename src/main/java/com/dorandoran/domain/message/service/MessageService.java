package com.dorandoran.domain.message.service;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.domain.message.dto.request.MessageSendRequest;
import com.dorandoran.domain.message.dto.response.MessageResponse;
import com.dorandoran.domain.message.entity.Message;
import com.dorandoran.domain.message.repository.MessageRepository;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.response.ErrorCode;
import com.dorandoran.standard.page.dto.PageMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MemberService memberService;

    @Transactional
    public void sendMessage(Long receiverId, String senderId, MessageSendRequest request) {
        Member receiver = memberService.findMemberById(receiverId);
        Member sender = memberService.findMemberByStringId(senderId);

        if (sender.getId().equals(receiver.getId())) {
            throw new CustomException(ErrorCode.CANNOT_SEND_MESSAGE_TO_SELF);
        }

        Message message = Message.createMessage(sender, receiver, request.getContent());
        messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessage(Long messageId, String memberId) {
        Member member = memberService.findMemberByStringId(memberId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(member.getId()) && !message.getReceiver().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.NO_ACCESS_TO_MESSAGE);
        }

        if ((message.getSender().getId().equals(member.getId()) && message.getSenderDeletedAt() != null) ||
                (message.getReceiver().getId().equals(member.getId()) && message.getReceiverDeletedAt() != null)) {
            throw new CustomException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        return MessageResponse.of(message);
    }

    public PageMessageDto<MessageResponse> getMessagesByType(String memberId, String type, int page, int size) {
        Member member = memberService.findMemberByStringId(memberId);

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Order.desc("createdAt")));

        Page<Message> messagePage = switch (type.toLowerCase()) {
            case "sent" -> messageRepository.findBySender(member, pageable);
            case "inbox" -> messageRepository.findByReceiver(member, pageable);
            default -> throw new CustomException(ErrorCode.INVALID_MESSAGE_TYPE);
        };

        Page<MessageResponse> responsePage = messagePage.map(MessageResponse::of);

        return new PageMessageDto<>(responsePage);
    }

    @Transactional
    public void deleteMessage(Long messageId, String memberId) {
        Member member = memberService.findMemberByStringId(memberId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorCode.MESSAGE_NOT_FOUND));

        if (message.getSender().getId().equals(member.getId())) {
            message.setSenderDeleted();
        } else if (message.getReceiver().getId().equals(member.getId())) {
            message.setReceiverDeleted();
        } else {
            throw new CustomException(ErrorCode.NO_ACCESS_TO_MESSAGE);
        }
    }
}
