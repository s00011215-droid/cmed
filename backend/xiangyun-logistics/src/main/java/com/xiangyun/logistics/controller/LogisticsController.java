package com.xiangyun.logistics.controller;

import com.xiangyun.common.result.Result;
import com.xiangyun.logistics.dto.LogisticsDTO;
import com.xiangyun.logistics.entity.LogisticsOrder;
import com.xiangyun.logistics.service.LogisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@Tag(name = "物流配送", description = "自動打單 / 軌跡查詢 / 簽收回調")
public class LogisticsController {
    private final LogisticsService logisticsService;

    @PostMapping("/api/v1/logistics/orders")
    @Operation(summary = "建立物流訂單（自動打單）")
    public Result<LogisticsOrder> create(@Valid @RequestBody LogisticsDTO.CreateRequest req) {
        return Result.ok(logisticsService.create(req));
    }

    @GetMapping("/api/v1/logistics/trace/{waybillNo}")
    @Operation(summary = "查詢物流軌跡")
    public Result<LogisticsOrder> getTrace(@PathVariable String waybillNo) {
        return Result.ok(logisticsService.getTrace(waybillNo));
    }

    @PostMapping("/api/v1/callback/logistics/trace")
    @Operation(summary = "物流軌跡回調（第三方 → 本系統）")
    public Result<Void> traceCallback(@Valid @RequestBody LogisticsDTO.TraceCallback callback) {
        logisticsService.handleTraceCallback(callback);
        return Result.ok();
    }
}
