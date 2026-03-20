package com.dorandoran.global.websocket.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.websocket")
public class WebSocketProperties {
    private String endpoint;
    private String[] allowedOrigins;
}
