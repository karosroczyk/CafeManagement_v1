package com.cafe.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    //@LoadBalanced
    public WebClient.Builder webClient() {
        return WebClient.builder();
    }
}