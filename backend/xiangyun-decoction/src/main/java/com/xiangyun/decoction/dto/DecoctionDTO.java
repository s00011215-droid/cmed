package com.xiangyun.decoction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DecoctionDTO {

    @Data @Schema(description = "下發煎藥訂單")
    public static class CreateRequest {
        @NotNull private Long prescriptionId;
        @NotBlank private String clinicCode;
        @Min(1) private Integer doseCount;
        @NotEmpty private List<DecoctionItem> items;
        private Map<String, Object> craft;
        private String deliveryOption;  // pickup/delivery
        private Map<String, Object> receiver;
    }

    @Data public static class DecoctionItem {
        private String materialName; private BigDecimal dosage;
        private String unit; private String decoctionNote;
    }

    @Data @Schema(description = "回調請求（第三方→本系統）")
    public static class StatusCallback {
        @NotBlank private String externalNo;
        @NotBlank private String prescriptionId;
        @NotBlank private String status;
        private String vacuumPkgNo;
        private String batchNo;
        private String message;
        private String operator;
        @NotBlank private String timestamp;
    }

    @Data @Schema(description = "煎藥訂單詳情")
    public static class DetailResponse {
        private Long id; private Long prescriptionId;
        private String externalNo; private String status;
        private Integer doseCount; private BigDecimal fee;
        private String vacuumPkgNo;
        private Map<String, Object> craft;
        private Map<String, Object> receiver;
        private List<Map<String, Object>> statusHistory;
    }
}
