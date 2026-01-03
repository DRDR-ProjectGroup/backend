package com.dorandoran.global.jwt;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class JwtProperties {
    @Value("${custom.jwt.secret}")
    private String secret;

    @Value("${custom.jwt.token.access-expiration}")
    private long accessExpiration;

    @Value("${custom.jwt.token.refresh-expiration}")
    private long refreshExpiration;
}