package com.xiangyun.risk.controller;

import com.xiangyun.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/risk")
@Tag(name = "風控服務", description = "敏感詞過濾 / 異地登入偵測 / 防刷單")
public class RiskController {

    @PostMapping("/check/content")
    @Operation(summary = "敏感詞檢測")
    public Result<Boolean> checkContent(@RequestBody String text) {
        // 調用敏感詞庫過濾
        return Result.ok(true);
    }
}
