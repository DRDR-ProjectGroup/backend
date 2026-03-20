package com.dorandoran.global.websocket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberListResponse {
    private List<ChatMemberInfo> users;

    public static MemberListResponse of(List<ChatMemberInfo> users) {
        return new MemberListResponse(users);
    }
}
