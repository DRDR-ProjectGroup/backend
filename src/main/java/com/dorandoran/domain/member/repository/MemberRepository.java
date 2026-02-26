package com.dorandoran.domain.member.repository;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.type.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    @Modifying
    @Query("DELETE FROM Member m WHERE m.status = :memberStatus AND m.deletedAt < :threshold")
    void deleteAllByStatusDeletedAndBefore(MemberStatus memberStatus, LocalDateTime threshold);
}
