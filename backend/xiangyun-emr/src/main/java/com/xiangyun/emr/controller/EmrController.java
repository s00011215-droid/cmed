package com.xiangyun.emr.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyun.common.result.Result;
import com.xiangyun.emr.dto.EmrDTO;
import com.xiangyun.emr.service.EmrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/emr") @RequiredArgsConstructor
@Tag(name = "電子病歷", description = "中醫 EMR：望聞問切 JSONB 記錄")
public class EmrController {

    private final EmrService emrService;
    private final HttpServletRequest request;
    @GetMapping
    @Operation(summary = "病歷分頁列表")
    public Result<Page<EmrDTO.ListItem>> list(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.ok(emrService.listAll(page, size));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "患者病歷列表")
    public Result<Page<EmrDTO.ListItem>> listByPatient(@PathVariable Long patientId,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.ok(emrService.listByPatient(patientId, page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "全文搜尋病歷")
    public Result<Page<EmrDTO.ListItem>> search(@RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        return Result.ok(emrService.search(keyword, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "病歷詳情（含四診 JSONB）")
    public Result<EmrDTO.DetailResponse> get(@PathVariable Long id) {
        return Result.ok(emrService.getDetail(id));
    }

    @PostMapping @Operation(summary = "建立/更新病歷")
    public Result<Long> save(@Valid @RequestBody EmrDTO.SaveRequest req) {
        if (req.getDoctorId() == null) {
            String uid = request.getHeader("X-User-Id");
            if (uid != null) req.setDoctorId(Long.valueOf(uid));
        }
        return Result.ok(emrService.save(req));
    }
}
