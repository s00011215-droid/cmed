package com.xiangyun.finance.controller;

import com.xiangyun.common.result.Result;
import com.xiangyun.finance.dto.FinanceDTO;
import com.xiangyun.finance.entity.Payment;
import com.xiangyun.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController @RequestMapping("/api/v1/finance") @RequiredArgsConstructor
@Tag(name = "財務結算", description = "支付 / 退款 / 每日結帳 / 三合一總帳")
public class FinanceController {
    private final FinanceService financeService;

    @PostMapping("/pay")
    @Operation(summary = "建立支付")
    public Result<Payment> pay(@Valid @RequestBody FinanceDTO.CreatePaymentRequest req) {
        return Result.ok(financeService.createPayment(req));
    }

    @PostMapping("/refund")
    @Operation(summary = "退款")
    public Result<Payment> refund(@Valid @RequestBody FinanceDTO.RefundRequest req) {
        return Result.ok(financeService.refund(req));
    }

    @GetMapping("/settlement/{date}")
    @Operation(summary = "每日結帳")
    public Result<FinanceDTO.DailySettlement> settlement(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(financeService.getDailySettlement(date));
    }

    @GetMapping("/prescription/{prescriptionId}")
    @Operation(summary = "查詢處方支付狀態")
    public Result<Payment> getByPrescription(@PathVariable Long prescriptionId) {
        return Result.ok(financeService.getByPrescription(prescriptionId));
    }
}
