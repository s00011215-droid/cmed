package com.xiangyun.admin.controller;

import com.xiangyun.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/admin")
@Tag(name = "雙後台", description = "診所端後台 + 平台端後台 / 權限管理 / 營運看板")
public class AdminController {

    @GetMapping("/dashboard")
    @Operation(summary = "營運看板")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(Map.of(
            "todayPatients", 42, "todayRevenue", 12800,
            "pendingPrescriptions", 5, "expiringInventory", 3));
    }

    @GetMapping("/roles")
    @Operation(summary = "角色權限列表")
    public Result<List<String>> roles() {
        return Result.ok(List.of("super_admin","admin","doctor","nurse","pharmacist","patient"));
    }
}
