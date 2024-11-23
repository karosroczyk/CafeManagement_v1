package com.cafe.apigateway;

import com.cafe.apigateway.filter.AuthenticationFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class APIGatewayApplication {
    public static void main(String[] args) {

        SpringApplication.run(APIGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder rlb, AuthenticationFilter
            authenticationFilter) {
        return rlb
                .routes()
                .route(p -> p
                        .path("/menu/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new
                                        AuthenticationFilter.Config())))
                        .uri("lb://inventory")
                )
//                .route(p -> p
//                        .path("/eureka/**")  // Exclude eureka from routing to avoid infinite loop
//                        .uri("http://localhost:8080/eureka/web") // Allow requests to eureka to be served directly
//                )
                .build();
    }

}