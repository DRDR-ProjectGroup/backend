package com.dorandoran.domain.message.repository;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.message.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.sender = :member AND m.senderDeletedAt IS NULL")
    Page<Message> findBySender(Member member, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.receiver = :member AND m.receiverDeletedAt IS NULL")
    Page<Message> findByReceiver(Member member, Pageable pageable);
}
