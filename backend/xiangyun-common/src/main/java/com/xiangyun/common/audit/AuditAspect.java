package com.xiangyun.common.audit;

import com.xiangyun.common.context.RlsContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * 審計 AOP 切面 — 自動記錄標記了 {@link Auditable} 的方法調用
 * <p>
 * 用於敏感操作審計（如查看患者病歷、匯出資料等）。
 * 成功和失敗都會記錄，失敗時記錄異常訊息。
 * </p>
 */
@Slf4j
@Aspect
@Component
public class AuditAspect {

    @Pointcut("@annotation(com.xiangyun.common.audit.Auditable)")
    public void auditableMethod() {
    }

    @AfterReturning(pointcut = "auditableMethod()")
    public void auditSuccess(JoinPoint joinPoint) {
        recordAudit(joinPoint, null);
    }

    @AfterThrowing(pointcut = "auditableMethod()", throwing = "ex")
    public void auditFailure(JoinPoint joinPoint, Throwable ex) {
        recordAudit(joinPoint, ex.getMessage());
    }

    private void recordAudit(JoinPoint joinPoint, String errorMsg) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Auditable annotation = method.getAnnotation(Auditable.class);

            RlsContext ctx = RlsContext.get();

            // 結構化日誌記錄（可由 ELK/Filebeat 採集到審計日誌表）
            log.info("AUDIT | action={} | target={} | userId={} | userName={} | "
                            + "clinicId={} | ip={} | success={} | error={} | time={}",
                    annotation.action(),
                    annotation.target(),
                    ctx != null ? ctx.getUserId() : "N/A",
                    ctx != null ? ctx.getUserName() : "N/A",
                    ctx != null ? ctx.getClinicId() : "N/A",
                    ctx != null ? ctx.getClientIp() : "N/A",
                    errorMsg == null,
                    errorMsg != null ? errorMsg : "",
                    Instant.now()
            );
        } catch (Exception e) {
            log.error("Failed to record audit log", e);
        }
    }
}
