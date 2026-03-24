package com.dorandoran.standard.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CookieDomainConfig {

    @Value("${cookie.domain}")
    private String cookieDomain;

    @PostConstruct
    public void init() {
        ControllerUt.cookieDomain = cookieDomain;
    }
}
