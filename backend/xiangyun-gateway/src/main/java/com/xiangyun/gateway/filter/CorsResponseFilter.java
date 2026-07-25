package com.xiangyun.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorsResponseFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, 
            exchange.getRequest().getHeaders().getOrigin());
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,OPTIONS");
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() { return -2; }
}
