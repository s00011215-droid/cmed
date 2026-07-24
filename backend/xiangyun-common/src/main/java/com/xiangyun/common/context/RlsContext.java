package com.xiangyun.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RLS（Row Level Security）上下文 — 每個請求的租戶/用戶資訊
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RlsContext {

    private Long clinicId;
    private Long userId;
    private String userName;
    private String role;
    private String clientIp;
    private String sessionId;
    private String userAgent;
    private String traceId;

    private static final ThreadLocal<RlsContext> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void set(RlsContext context) { CONTEXT_HOLDER.set(context); }
    public static RlsContext get() { return CONTEXT_HOLDER.get(); }
    public static void clear() { CONTEXT_HOLDER.remove(); }

    public static Long currentClinicId() {
        RlsContext ctx = get();
        return ctx != null ? ctx.getClinicId() : null;
    }
    public static Long currentUserId() {
        RlsContext ctx = get();
        return ctx != null ? ctx.getUserId() : null;
    }
    public static String currentUserName() {
        RlsContext ctx = get();
        return ctx != null ? ctx.getUserName() : null;
    }
    public static String currentTraceId() {
        RlsContext ctx = get();
        return ctx != null ? ctx.getTraceId() : null;
    }
}
