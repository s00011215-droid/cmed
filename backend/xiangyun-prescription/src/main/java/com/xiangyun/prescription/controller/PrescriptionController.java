package com.xiangyun.prescription.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyun.common.result.Result;
import com.xiangyun.prescription.dto.PrescriptionDTO;
import com.xiangyun.prescription.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/prescription") @RequiredArgsConstructor
@Tag(name = "電子處方", description = "處方 CRUD + 配伍禁忌校驗 + 電子簽章 + 狀態機")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final HttpServletRequest request;

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "患者處方列表")
    public Result<Page<PrescriptionDTO.ListItem>> listByPatient(@PathVariable Long patientId,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.ok(prescriptionService.listByPatient(patientId, page, size));
    }

    @GetMapping("/{id}")
    public Result<PrescriptionDTO.DetailResponse> get(@PathVariable Long id) {
        return Result.ok(prescriptionService.getDetail(id));
    }

    @PostMapping
    public Result<PrescriptionDTO.DetailResponse> save(@Valid @RequestBody PrescriptionDTO.SaveRequest req) {
        if (req.getDoctorId() == null) {
            String uid = request.getHeader("X-User-Id");
            if (uid != null) req.setDoctorId(Long.valueOf(uid));
        }
        if (req.getClinicId() == null) {
            String cid = request.getHeader("X-Clinic-Id");
            if (cid != null) req.setClinicId(Long.valueOf(cid));
        }
        return Result.ok(prescriptionService.save(req));
    }

    @PostMapping("/{id}/transition")
    @Operation(summary = "處方狀態流轉")
    public Result<Void> transition(@PathVariable Long id, @Valid @RequestBody PrescriptionDTO.StatusTransition req) {
        prescriptionService.transition(id, req);
        return Result.ok();
    }
}
