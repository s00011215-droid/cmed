package com.xiangyun.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 統一 API 回應封裝
 *
 * @param <T> 資料類型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "統一 API 回應")
public class Result<T> {

    /** 業務狀態碼（0 = 成功） */
    @Schema(description = "狀態碼", example = "0")
    private int code;

    /** 訊息 */
    @Schema(description = "回應訊息", example = "success")
    private String message;

    /** 資料 */
    @Schema(description = "回應資料")
    private T data;

    /** 請求追蹤 ID */
    @Schema(description = "請求追蹤 ID")
    private String traceId;

    /** 時間戳 */
    @Schema(description = "回應時間")
    private Instant timestamp;

    // ================================================================
    // 工廠方法
    // ================================================================

    public static <T> Result<T> ok() {
        return Result.<T>builder()
                .code(0)
                .message("success")
                .timestamp(Instant.now())
                .build();
    }

    public static <T> Result<T> ok(T data) {
        return Result.<T>builder()
                .code(0)
                .message("success")
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> Result<T> ok(String message, T data) {
        return Result.<T>builder()
                .code(0)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> Result<T> fail(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> Result<T> fail(String message) {
        return Result.<T>builder()
                .code(-1)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public boolean isSuccess() {
        return code == 0;
    }
}
