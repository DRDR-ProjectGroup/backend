package com.dorandoran.global.websocket.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class StompSessionRegistry {
    // memberId -> sessionIds
    private final Map<String, Set<String>> memberSessions = new ConcurrentHashMap<>();

    // sessionId -> memberId
    private final Map<String, String> sessionUser = new ConcurrentHashMap<>();

    public void addSession(String memberId, String sessionId) {
        memberSessions.computeIfAbsent(memberId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionUser.put(sessionId, memberId);
    }

    public String removeSession(String sessionId) {
        String memberId = sessionUser.remove(sessionId);

        if (memberId != null) {
            Set<String> sessions = memberSessions.get(memberId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    memberSessions.remove(memberId);
                }
            }
        }

        return memberId;
    }

    public int getSessionCount(String memberId) {
        return memberSessions.getOrDefault(memberId, Set.of()).size();
    }

    public Set<String> getConnectedMemberIds() {
        return memberSessions.keySet();
    }
}
