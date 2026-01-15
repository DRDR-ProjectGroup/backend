package com.dorandoran.domain.member.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {
    ACTIVE("활성"),
    BLOCKED("차단"),
    DELETED("탈퇴");

    private final String description;

    @JsonCreator
    public static MemberStatus from(String value) {
        return MemberStatus.valueOf(value.toUpperCase());
    }
}
