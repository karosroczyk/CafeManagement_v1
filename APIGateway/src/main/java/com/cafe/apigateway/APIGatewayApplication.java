package com.cafe.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class APIGatewayApplication {
    public static void main(String[] args) {

        SpringApplication.run(APIGatewayApplication.class, args);
    }

}