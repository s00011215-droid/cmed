package com.xiangyun.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 全域鑑權過濾器
 * <p>
 * 在 Gateway 層統一驗證 JWT Token，解析出 clinicId/userId/role 後
 * 以 Request Header 傳遞給下游微服務。
 * </p>
 */
@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret:xiangyun-zhifang-jwt-secret-key-min-256-bits-change-in-production!!}")
    private String jwtSecret;

    /** 不需要 JWT 校驗的路徑（白名單） */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/v1/account/login",
            "/api/v1/account/register",
            "/api/v1/account/refresh",
            "/api/v1/callback/",           // 第三方回調
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名單直接放行
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        // 提取 Token
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "缺少 Authorization Header");
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 提取用戶資訊，以 Header 傳遞給下游服務
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(claims.get("userId", Long.class)))
                    .header("X-User-Name", claims.get("userName", String.class))
                    .header("X-Clinic-Id", String.valueOf(claims.get("clinicId", Long.class)))
                    .header("X-Role", claims.get("role", String.class))
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange);

        } catch (JwtException e) {
            log.warn("JWT validation failed: {} — path={}", e.getMessage(), path);
            return unauthorized(exchange, "Token 無效或已過期");
        }
    }

    @Override
    public int getOrder() {
        return -100; // 高優先級
    }

    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
