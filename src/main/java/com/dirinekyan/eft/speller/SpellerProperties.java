package com.dirinekyan.eft.speller;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.speller")
public record SpellerProperties(String baseUrl, int maxTextLength, Duration connectTimeout, Duration readTimeout) {
}
