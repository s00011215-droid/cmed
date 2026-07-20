package com.xiangyun.common.interceptor;

import com.xiangyun.common.context.RlsContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * RLS 上下文過濾器 — 從 JWT Token 解析租戶/用戶資訊
 * <p>
 * 在請求進入 Controller 前，從 Authorization Header 中解析 JWT，
 * 提取 clinic_id / user_id / role 並注入到 {@link RlsContext} ThreadLocal。
 * 請求結束後自動清理。
 * </p>
 * <p>
 * 注意：此 Filter 需要在 Spring Security Filter Chain 之後執行，
 * 確保 Token 已經被驗證。
 * </p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class RlsContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        // 注入 MDC（日誌追蹤）
        MDC.put("traceId", traceId);

        try {
            // 從 JWT 解析的 attributes 中讀取（由 Spring Security 或 Gateway 設定）
            RlsContext context = RlsContext.builder()
                    .clinicId(parseLong(request.getAttribute("clinicId")))
                    .userId(parseLong(request.getAttribute("userId")))
                    .userName(parseString(request.getAttribute("userName")))
                    .role(parseString(request.getAttribute("role")))
                    .clientIp(getClientIp(request))
                    .sessionId(request.getSession().getId())
                    .userAgent(request.getHeader("User-Agent"))
                    .traceId(traceId)
                    .build();

            RlsContext.set(context);
            MDC.put("userId", String.valueOf(context.getUserId()));
            MDC.put("clinicId", String.valueOf(context.getClinicId()));

            log.debug("RLS Context set: clinicId={}, userId={}, role={}",
                    context.getClinicId(), context.getUserId(), context.getRole());

            filterChain.doFilter(request, response);

        } finally {
            RlsContext.clear();
            MDC.clear();
        }
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseString(Object value) {
        return value != null ? value.toString() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多層代理時取第一個 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
