package com.xiangyun.common.audit;

import java.lang.annotation.*;

/**
 * 審計註解 — 標記需要自動審計的方法
 * <p>
 * 標記此註解的方法調用會被 {@link AuditAspect} 自動攔截並記錄審計日誌。
 * 適用於：查看敏感資料、匯出、權限變更等操作。
 * </p>
 *
 * <pre>{@code
 * @Auditable(action = "VIEW_SENSITIVE", target = "EMR")
 * public EmrDetailDto getEmrDetail(Long emrId) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /** 操作類型 */
    String action();

    /** 目標資源 */
    String target() default "";
}
