package com.cafe.apigateway.config;

import com.cafe.apigateway.filter.AuthenticationFilter;
import com.cafe.apigateway.filter.RouteValidator;
import com.cafe.apigateway.util.JwtUtil;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClient() {
        return WebClient.builder();
    }

//    @Bean
//    public RouteLocator routeLocator(RouteLocatorBuilder rlb, AuthenticationFilter
//            authenticationFilter) {
//        return rlb
//                .routes()
//                .route(r -> r
//                        .path("menu/**")
//                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
//                        .uri("lb://menu"))
//                .build();
//
//        //                .route(p -> p
////                        .path("/menu/**")
////                        .filters(f -> f.filter(authenticationFilter.apply(new
////                                        AuthenticationFilter.Config())))
////                        .uri("lb://menu/menuss")
////                )
//    }
}