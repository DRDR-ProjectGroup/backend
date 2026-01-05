package com.dorandoran.domain.member.dto.request;

import com.dorandoran.global.annotation.email.NaverEmail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NaverEmail
    private String email;
}
