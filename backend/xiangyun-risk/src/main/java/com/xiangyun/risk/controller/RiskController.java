package com.xiangyun.risk.controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/risk")
public class RiskController {
    @GetMapping("/health") public Map<String,String> health() { return Map.of("status","UP","service","risk"); }
    @GetMapping("/ping") public Map<String,Object> ping() { return Map.of("ok",true,"timestamp",System.currentTimeMillis()); }
}
