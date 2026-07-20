package com.xiangyun.notify.controller;

import com.xiangyun.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/v1/notify")
@Tag(name = "通知服務", description = "Web Push / 短信 / 站內通知")
public class NotifyController {

    @PostMapping("/send")
    @Operation(summary = "發送通知")
    public Result<Void> send(@RequestBody Map<String, Object> req) {
        return Result.ok(); // 實際通過 MQ 非同步發送
    }
}
