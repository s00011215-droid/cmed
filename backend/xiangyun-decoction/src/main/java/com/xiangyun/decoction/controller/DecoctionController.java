package com.xiangyun.decoction.controller;

import com.xiangyun.common.result.Result;
import com.xiangyun.decoction.dto.DecoctionDTO;
import com.xiangyun.decoction.entity.DecoctionOrder;
import com.xiangyun.decoction.service.DecoctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@Tag(name = "煎藥中心", description = "煎藥訂單下發 / 回調處理 / 狀態查詢")
public class DecoctionController {
    private final DecoctionService decoctionService;

    @PostMapping("/api/v1/decoction/orders")
    @Operation(summary = "下發煎藥訂單")
    public Result<DecoctionOrder> create(@Valid @RequestBody DecoctionDTO.CreateRequest req) {
        return Result.ok(decoctionService.create(req));
    }

    @GetMapping("/api/v1/decoction/orders/{id}")
    @Operation(summary = "查詢煎藥訂單")
    public Result<DecoctionOrder> get(@PathVariable Long id) {
        return Result.ok(decoctionService.getById(id));
    }

    @PostMapping("/api/v1/decoction/orders/{id}/cancel")
    @Operation(summary = "取消煎藥訂單")
    public Result<Void> cancel(@PathVariable Long id) {
        decoctionService.cancel(id);
        return Result.ok();
    }

    @PostMapping("/api/v1/callback/decoction/status")
    @Operation(summary = "煎藥狀態回調（第三方 → 本系統）", description = "HMAC-SHA256 簽名驗證 + 冪等保護")
    public Result<Void> statusCallback(@Valid @RequestBody DecoctionDTO.StatusCallback callback,
                                        @RequestHeader("X-Signature") String signature) {
        decoctionService.handleCallback(callback, signature);
        return Result.ok();
    }
}
