package com.xiangyun.account.controller;

import com.xiangyun.account.dto.AccountDTO;
import com.xiangyun.account.service.AccountService;
import com.xiangyun.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Tag(name = "帳號服務", description = "SSO 登入 / 註冊 / Token 刷新")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/login")
    @Operation(summary = "用戶登入", description = "使用用戶名和密碼登入，返回 JWT Token")
    public Result<AccountDTO.LoginResponse> login(@Valid @RequestBody AccountDTO.LoginRequest request) {
        return Result.ok(accountService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "用戶註冊")
    public Result<AccountDTO.LoginResponse> register(@Valid @RequestBody AccountDTO.RegisterRequest request) {
        return Result.ok(accountService.register(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用 Refresh Token 獲取新的 Access Token")
    public Result<AccountDTO.LoginResponse> refresh(@Valid @RequestBody AccountDTO.RefreshRequest request) {
        return Result.ok(accountService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出", description = "使當前 Token 失效")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        accountService.logout(token);
        return Result.ok();
    }
}
