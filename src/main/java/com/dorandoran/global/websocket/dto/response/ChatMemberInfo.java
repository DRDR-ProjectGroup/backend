package com.dorandoran.global.websocket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMemberInfo {
    private String memberId;
    private String nickname;

    public static ChatMemberInfo of(String memberId, String nickname) {
        return ChatMemberInfo.builder()
                .memberId(memberId)
                .nickname(nickname)
                .build();
    }
}
