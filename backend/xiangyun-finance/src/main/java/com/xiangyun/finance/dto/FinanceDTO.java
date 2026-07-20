package com.xiangyun.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

public class FinanceDTO {
    @Data @Schema(description = "建立支付")
    public static class CreatePaymentRequest {
        @NotNull private Long prescriptionId;
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
        @NotBlank private String method;
    }

    @Data @Schema(description = "退款請求")
    public static class RefundRequest {
        @NotNull private Long paymentId;
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
        private String reason;
    }

    @Data @Schema(description = "每日結帳")
    public static class DailySettlement {
        private LocalDate date;
        private BigDecimal totalRevenue;
        private int prescriptionCount;
        private BigDecimal cashAmount;
        private BigDecimal onlineAmount;
        private BigDecimal insuranceAmount;
        private BigDecimal refundAmount;
    }
}
