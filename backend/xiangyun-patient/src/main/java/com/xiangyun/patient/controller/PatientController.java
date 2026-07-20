package com.xiangyun.patient.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyun.common.result.Result;
import com.xiangyun.patient.dto.PatientDTO;
import com.xiangyun.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/patient") @RequiredArgsConstructor
@Tag(name = "患者服務", description = "患者 CRUD + 家庭成員管理")
public class PatientController {
    private final PatientService patientService;

    @GetMapping
    @Operation(summary = "搜尋患者")
    public Result<Page<PatientDTO.ListItem>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(patientService.search(keyword, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "患者詳情")
    public Result<PatientDTO.DetailResponse> get(@PathVariable Long id) {
        return Result.ok(patientService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增/更新患者")
    public Result<Long> save(@Valid @RequestBody PatientDTO.SaveRequest req) {
        return Result.ok(patientService.save(req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "軟刪除患者")
    public Result<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/family")
    @Operation(summary = "綁定家庭成員")
    public Result<Void> bindFamily(@PathVariable Long id, @Valid @RequestBody PatientDTO.FamilyBindRequest req) {
        patientService.bindFamily(id, req);
        return Result.ok();
    }

    @DeleteMapping("/{id}/family/{memberId}")
    @Operation(summary = "解除家庭成員綁定")
    public Result<Void> unbindFamily(@PathVariable Long id, @PathVariable Long memberId) {
        patientService.unbindFamily(id, memberId);
        return Result.ok();
    }
}
