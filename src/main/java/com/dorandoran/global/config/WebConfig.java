package com.dorandoran.global.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String userHome = System.getProperty("user.home");
        String mediaLocation = "file:" + userHome + "/doranTemp/";

        registry.addResourceHandler("/media/**")
                .addResourceLocations(mediaLocation);
    }
}
