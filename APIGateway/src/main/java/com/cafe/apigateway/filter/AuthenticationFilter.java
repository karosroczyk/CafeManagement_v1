package com.cafe.apigateway.filter;

import com.cafe.apigateway.util.JwtUtil;
import com.netflix.discovery.EurekaClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;
    @Autowired
    private JwtUtil jwtUtil;
//    @Autowired
//    private WebClient.Builder webClientBuilder;
//    @Autowired
//    private EurekaClient discoveryClient;
//    private String userServiceUrl;

    public AuthenticationFilter() {
        super(Config.class);
        System.out.println("AuthenticationFilter initialized"); // Debug log
    }

//    @PostConstruct
//    private void init() {
//        userServiceUrl = discoveryClient.getNextServerFromEureka("user", false).getHomePageUrl();
//    }

    @Override
    public GatewayFilter apply(Config config) {
        System.out.println("AuthenticationFilter applied to route");
        return ((exchange, chain) -> {
            System.out.println("Processing request: " + exchange.getRequest().getURI());
            if (validator.isSecured.test(exchange.getRequest())) {
                System.out.println("Secured route detected");
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    System.out.println("Missing Authorization header");
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                } else {
                    System.out.println("Invalid Authorization header format");
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Authorization header format");
                }

                try {
                    jwtUtil.validateToken(authHeader);
                    System.out.println("Token validated successfully");
                } catch (Exception e) {
                    System.out.println("Token validation failed");
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {

    }
}
