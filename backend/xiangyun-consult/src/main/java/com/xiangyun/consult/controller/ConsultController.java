package com.xiangyun.consult.controller;

import com.xiangyun.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/consult")
public class ConsultController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "timestamp", System.currentTimeMillis(), "service", "consult");
    }

    @GetMapping("/rooms")
    public Result<List<Map<String, Object>>> rooms() {
        return Result.ok(List.of(
            Map.of("id", 1, "name", "诊室一", "status", "available"),
            Map.of("id", 2, "name", "诊室二", "status", "occupied"),
            Map.of("id", 3, "name", "诊室三", "status", "available")
        ));
    }

    @GetMapping("/queue")
    public Result<Map<String, Object>> queue() {
        return Result.ok(Map.of(
            "waitingCount", 5,
            "currentPatient", "张三",
            "estimatedWaitMinutes", 15
        ));
    }
}
