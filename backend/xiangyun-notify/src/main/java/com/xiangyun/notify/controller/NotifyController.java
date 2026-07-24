package com.xiangyun.notify.controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/notify")
public class NotifyController {
    @GetMapping("/health") public Map<String,String> health() { return Map.of("status","UP","service","notify"); }
    @GetMapping("/ping") public Map<String,Object> ping() { return Map.of("ok",true,"timestamp",System.currentTimeMillis()); }
}
