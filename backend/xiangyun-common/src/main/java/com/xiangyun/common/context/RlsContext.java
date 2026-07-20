package com.xiangyun.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RLS（Row Level Security）上下文 — 每個請求的租戶/用戶資訊
 * <p>
 * 由 {@link RlsContextFilter} 從 JWT 中解析並注入到 {@link ThreadLocal}，
 * 統一在每次 DB 操作前透過 {@code SET LOCAL app.clinic_id = ?} 設定。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RlsContext {

    /** 診所 ID（租戶隔離主鍵） */
    private Long clinicId;

    /** 當前用戶 ID */
    private Long userId;

    /** 當前用戶名（用於審計日誌冗餘） */
    private String userName;

    /** 用戶角色 */
    private String role;

    /** 客戶端 IP */
    private String clientIp;

    /** 請求 Session ID */
    private String sessionId;

    /** User-Agent */
    private String userAgent;

    /** 請求追蹤 ID（全鏈路） */
    private String traceId;

    // ================================================================
    // ThreadLocal 持有
    // ================================================================

    private static final ThreadLocal<RlsContext> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void set(RlsContext context) {
        CONTEXT_HOLDER.set(context);
    }

    public static RlsContext get() {
        return CONTEXT_HOLDER.get();
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    // ================================================================
    // 便捷方法
    // ================================================================

    public static Long getClinicId() {
        RlsContext ctx = get();
        return ctx != null ? ctx.getClinicId() : null;
    }

    public static Long getUserId() {
        RlsContext ctx = get();
        return ctx != null ? ctx.getUserId() : null;
    }

    public static String getUserName() {
        RlsContext ctx = get();
        return ctx != null ? ctx.getUserName() : null;
    }

    public static String getTraceId() {
        RlsContext ctx = get();
        return ctx != null ? ctx.getTraceId() : null;
    }
}
