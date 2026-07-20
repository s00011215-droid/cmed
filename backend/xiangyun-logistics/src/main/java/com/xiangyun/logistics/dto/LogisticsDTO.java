package com.xiangyun.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import java.util.Map;

public class LogisticsDTO {
    @Data @Schema(description = "建立物流訂單")
    public static class CreateRequest {
        @NotNull private Long decoctionId;
        private String carrier;
        @NotNull private Map<String, Object> receiver;
        private Map<String, Object> packageInfo;
    }

    @Data @Schema(description = "軌跡回調")
    public static class TraceCallback {
        @NotBlank private String waybillNo;
        @NotBlank private String status;
        private List<Map<String, Object>> traces;
        private String signedBy; private String signedAt;
        private String exceptionReason;
        @NotBlank private String timestamp;
    }

    @Data @Schema(description = "物流詳情")
    public static class DetailResponse {
        private Long id; private Long decoctionId;
        private String carrier; private String carrierName;
        private String waybillNo; private String electronicSheet;
        private String status;
        private Map<String, Object> receiver;
        private Map<String, Object> latestTrace;
        private List<Map<String, Object>> traceHistory;
        private String estimatedDelivery; private String signedAt;
    }
}
