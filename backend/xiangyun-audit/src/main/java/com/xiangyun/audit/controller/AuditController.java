package com.xiangyun.audit.controller;

import com.xiangyun.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController @RequestMapping("/api/v1/audit")
@Tag(name = "審計服務", description = "審計日誌查詢 / 異常訪問檢測")
public class AuditController {

    @GetMapping("/logs")
    @Operation(summary = "查詢審計日誌")
    public Result<Map<String, Object>> queryLogs(@RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action) {
        return Result.ok(Map.of("total", 0, "records", List.of()));
    }

    @GetMapping("/anomalies")
    @Operation(summary = "異常訪問檢測")
    public Result<List<Map<String, Object>>> anomalies() {
        return Result.ok(List.of(Map.of(
            "type", "cross_clinic_access", "userId", 12345,
            "patientId", 67890, "time", LocalDateTime.now().toString())));
    }
}
