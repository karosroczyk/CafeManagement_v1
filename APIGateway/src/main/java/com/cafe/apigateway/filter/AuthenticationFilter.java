package com.cafe.apigateway.filter;

import com.cafe.apigateway.util.JwtUtil;
import com.netflix.discovery.EurekaClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@RefreshScope
@Component
public class AuthenticationFilter  extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private EurekaClient discoveryClient;
    private String userServiceUrl;

    public AuthenticationFilter(RouteValidator validator, JwtUtil jwtUtil, WebClient.Builder webClientBuilder, EurekaClient discoveryClient) {
        this. validator = validator;
        this.jwtUtil = jwtUtil;
        this.webClientBuilder = webClientBuilder;
        this.discoveryClient = discoveryClient;
        System.out.println("AuthenticationFilter initialized");
    }

    public AuthenticationFilter() {
        super(Config.class);
        System.out.println("AuthenticationFilter initialized in default");
    }
    @PostConstruct
    private void init() {
        userServiceUrl = discoveryClient.getNextServerFromEureka("user", false).getHomePageUrl();
    }

    @Override
    public GatewayFilter apply(Config config) {
        System.out.println("AuthenticationFilter applied to route");
        return (exchange, chain) -> filter(exchange, chain);
    }

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("Processing request: " + exchange.getRequest().getURI());

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Invalid Authorization header format");
            return Mono.error(new RuntimeException("Invalid Authorization header format"));
        }

        String token = authHeader.substring(7).trim();  // Remove the "Bearer " prefix

        try {
//            String validationUrl = "http://localhost:8080/user/auth/validate?token=" + token;
//            String response = webClientBuilder.build().get()
//                    .uri(validationUrl)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();

            jwtUtil.validateToken(token);

            //System.out.println("Token validation response from AuthService: " + response);
            System.out.println("Token validated successfully");
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return Mono.error(new RuntimeException("Token validation failed"));
        }
        return chain.filter(exchange);
    }

    public static class Config {

    }
}
