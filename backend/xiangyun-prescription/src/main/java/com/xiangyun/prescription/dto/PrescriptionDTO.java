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
        public Long id;
        @NotNull public Long patientId;
        public Long doctorId;
        public Long clinicId;
        public Long emrId;
        @NotBlank public String visitType;
        @Min(1) public Integer doseCount;
        @Min(1) public Integer doseDays;
        @NotEmpty @Valid public List<PrescriptionItem> items;
        public String decoctionMethod;
        public String deliveryOption;
        public String diagnosisCode;
    }

    @Data @Schema(description = "處方列表項") public static class ListItem {
        public Long id; public String prescriptionNo; public Long patientId;
        public Long doctorId; public String status;
        public Integer doseCount; public BigDecimal totalAmount;
        public String decoctionMethod; public String signStatus;
        public OffsetDateTime createdAt;
    }

    @Data @Schema(description = "處方詳情") public static class DetailResponse {
        public Long id; public String prescriptionNo;
        public Long patientId; public Long doctorId; public Long emrId;
        public String visitType; public String status;
        public Integer doseCount; public Integer doseDays;
        public List<PrescriptionItem> items;
        public BigDecimal totalAmount; public BigDecimal decoctionFee; public BigDecimal deliveryFee;
        public String decoctionMethod; public String deliveryOption;
        public String diagnosisCode; public String signStatus;
        public OffsetDateTime signedAt; public String signAlgorithm;
        public List<String> warnings;
        public OffsetDateTime createdAt;
    }

    @Data @Schema(description = "處方狀態流轉") public static class StatusTransition {
        @NotBlank public String status;
        public String comment;
    }
}
