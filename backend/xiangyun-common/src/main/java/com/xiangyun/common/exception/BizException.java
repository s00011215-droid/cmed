package com.xiangyun.common.exception;

import lombok.Getter;

/**
 * 業務異常 — 統一錯誤碼體系
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        super(message);
        this.code = -1;
    }

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + ": " + detail);
        this.code = errorCode.getCode();
    }

    // ================================================================
    // 常用錯誤碼枚舉
    // ================================================================

    @Getter
    public enum ErrorCode {
        // 通用
        SUCCESS(0, "success"),
        SYSTEM_ERROR(500, "系統內部錯誤"),
        INVALID_PARAM(400, "參數錯誤"),
        UNAUTHORIZED(401, "未授權"),
        FORBIDDEN(403, "無權限"),
        NOT_FOUND(404, "資源不存在"),
        CONFLICT(409, "資源衝突"),
        TOO_MANY_REQUESTS(429, "請求過於頻繁"),

        // 業務
        USER_NOT_FOUND(1001, "用戶不存在"),
        PASSWORD_ERROR(1002, "密碼錯誤"),
        ACCOUNT_DISABLED(1003, "帳號已停用"),
        TOKEN_EXPIRED(1004, "Token 已過期"),
        TOKEN_INVALID(1005, "Token 無效"),

        PATIENT_NOT_FOUND(2001, "患者不存在"),
        DOCTOR_NOT_FOUND(2101, "醫生不存在"),
        SCHEDULE_FULL(2102, "排班已滿"),

        PRESCRIPTION_NOT_FOUND(3001, "處方不存在"),
        PRESCRIPTION_ALREADY_SIGNED(3002, "處方已簽署"),
        INCOMPATIBILITY_DETECTED(3003, "配伍禁忌警告"),
        INVALID_STATUS_TRANSITION(3004, "狀態轉換不合法"),

        INVENTORY_INSUFFICIENT(4001, "庫存不足"),
        INVENTORY_LOCKED(4002, "庫存已被鎖定"),

        DECOCTION_ORDER_EXISTS(5001, "已有進行中的煎藥訂單"),
        DECOCTION_CANCEL_FAILED(5002, "煎藥訂單不可取消"),

        LOGISTICS_ORDER_FAILED(6001, "物流下單失敗"),

        SIGNATURE_VERIFY_FAILED(7001, "簽章驗證失敗"),
        CERTIFICATE_EXPIRED(7002, "憑證已過期"),
        ;

        private final int code;
        private final String message;

        ErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
