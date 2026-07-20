package com.xiangyun.insurance.controller;

import com.xiangyun.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/v1/insurance")
@Tag(name = "醫保對接", description = "門診結算 / 處方上傳")
public class InsuranceController {
    @PostMapping("/claim") @Operation(summary = "醫保結算")
    public Result<Map<String, String>> claim(@RequestBody Map<String, Object> req) {
        return Result.ok(Map.of("status", "submitted", "claimId", "CL-20260720-001"));
    }
}
