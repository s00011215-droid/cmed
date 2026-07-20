package com.xiangyun.his.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class HisDTO {

    @Data @Schema(description = "掛號請求")
    public static class RegistrationRequest {
        @NotNull private Long patientId;
        @NotNull private Long doctorId;
        @NotNull private LocalDate date;
        @NotNull private Integer timeSlot;    // 1=上午 2=下午 3=晚上
        private Long scheduleId;               // 排班 ID（可選）
    }

    @Data @Schema(description = "掛號記錄")
    public static class Registration {
        private Long id; private Long patientId; private String patientName;
        private Long doctorId; private String doctorName;
        private LocalDate date; private Integer timeSlot;
        private String status;                 // registered / consulted / cancelled
        private BigDecimal registrationFee;
    }

    @Data @Schema(description = "收費請求")
    public static class BillingRequest {
        @NotNull private Long prescriptionId;
        @NotNull private String paymentMethod; // cash/octopus/alipay/wechat/credit_card/insurance
        private BigDecimal amount;             // 可選，預設取處方總額
    }

    @Data @Schema(description = "藥房調劑請求")
    public static class DispenseRequest {
        @NotNull private Long prescriptionId;
        private String pharmacistNote;
    }

    @Data @Schema(description = "就診隊列項")
    public static class QueueItem {
        private Long registrationId; private Long patientId; private String patientName;
        private String status;  // waiting / in_consultation / completed
        private Integer queueNumber;
    }
}
