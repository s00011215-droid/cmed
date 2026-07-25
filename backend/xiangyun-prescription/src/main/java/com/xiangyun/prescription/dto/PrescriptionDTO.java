package com.xiangyun.prescription.dto;

import com.xiangyun.prescription.entity.Prescription.PrescriptionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

public class PrescriptionDTO {

    @Data @Schema(description = "建立/更新處方")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SaveRequest {
        private Long id;
        @NotNull private Long patientId; @NotNull private Long doctorId;
        private Long emrId;
        @NotBlank private String visitType;
        @Min(1) private Integer doseCount;
        @Min(1) private Integer doseDays;
        @NotEmpty @Valid private List<PrescriptionItem> items;
        private String decoctionMethod;  // self / center
        private String deliveryOption;   // pickup / delivery
        private String diagnosisCode;
    }

    @Data @Schema(description = "處方列表項") public static class ListItem {
        private Long id; private String prescriptionNo; private Long patientId;
        private Long doctorId; private String status;
        private Integer doseCount; private BigDecimal totalAmount;
        private String decoctionMethod; private String signStatus;
        private OffsetDateTime createdAt;
    }

    @Data @Schema(description = "處方詳情") public static class DetailResponse {
        private Long id; private String prescriptionNo;
        private Long patientId; private Long doctorId; private Long emrId;
        private String visitType; private String status;
        private Integer doseCount; private Integer doseDays;
        private List<PrescriptionItem> items;
        private BigDecimal totalAmount; private BigDecimal decoctionFee; private BigDecimal deliveryFee;
        private String decoctionMethod; private String deliveryOption;
        private String diagnosisCode; private String signStatus;
        private OffsetDateTime signedAt; private String signAlgorithm;
        private List<String> warnings;  // 配伍禁忌警告
        private OffsetDateTime createdAt;
    }

    @Data @Schema(description = "處方狀態流轉") public static class StatusTransition {
        @NotBlank private String status;  // 目標狀態
        private String comment;
    }
}
