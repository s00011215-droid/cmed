package com.xiangyun.his.controller;

import com.xiangyun.common.result.Result;
import com.xiangyun.his.dto.HisDTO;
import com.xiangyun.his.service.HisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/his") @RequiredArgsConstructor
@Tag(name = "診所門診 HIS", description = "掛號 / 就診隊列 / 收費收銀 / 藥房核銷")
public class HisController {
    private final HisService hisService;

    @PostMapping("/register")
    @Operation(summary = "掛號分診")
    public Result<HisDTO.Registration> register(@Valid @RequestBody HisDTO.RegistrationRequest req) {
        return Result.ok(hisService.register(req));
    }

    @GetMapping("/queue/{doctorId}")
    @Operation(summary = "就診隊列")
    public Result<List<HisDTO.QueueItem>> getQueue(@PathVariable Long doctorId) {
        return Result.ok(hisService.getQueue(doctorId));
    }

    @PostMapping("/bill")
    @Operation(summary = "收費收銀")
    public Result<HisDTO.BillingRequest> bill(@Valid @RequestBody HisDTO.BillingRequest req) {
        return Result.ok(hisService.bill(req));
    }

    @PostMapping("/dispense")
    @Operation(summary = "藥房核銷")
    public Result<HisDTO.DispenseRequest> dispense(@Valid @RequestBody HisDTO.DispenseRequest req) {
        return Result.ok(hisService.dispense(req));
    }
}
