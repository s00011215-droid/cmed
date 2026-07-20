package com.xiangyun.prescription.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyun.common.result.Result;
import com.xiangyun.prescription.dto.PrescriptionDTO;
import com.xiangyun.prescription.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/prescription") @RequiredArgsConstructor
@Tag(name = "電子處方", description = "處方 CRUD + 配伍禁忌校驗 + 電子簽章 + 狀態機")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "患者處方列表")
    public Result<Page<PrescriptionDTO.ListItem>> listByPatient(@PathVariable Long patientId,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.ok(prescriptionService.listByPatient(patientId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "處方詳情（含配伍禁忌警告）")
    public Result<PrescriptionDTO.DetailResponse> get(@PathVariable Long id) {
        return Result.ok(prescriptionService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "建立/編輯處方（自動計算金額 + 配伍禁忌檢查）")
    public Result<PrescriptionDTO.DetailResponse> save(@Valid @RequestBody PrescriptionDTO.SaveRequest req) {
        return Result.ok(prescriptionService.save(req));
    }

    @PostMapping("/{id}/transition")
    @Operation(summary = "處方狀態流轉", description = "draft→pending_review→approved→paid→dispensing→completed")
    public Result<Void> transition(@PathVariable Long id, @Valid @RequestBody PrescriptionDTO.StatusTransition req) {
        prescriptionService.transition(id, req);
        return Result.ok();
    }
}
