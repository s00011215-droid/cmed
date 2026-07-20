package com.xiangyun.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AccountDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "登入請求")
    public static class LoginRequest {
        @NotBlank @Size(min = 3, max = 64)
        @Schema(description = "用戶名", example = "dr_chan")
        private String username;

        @NotBlank @Size(min = 6, max = 128)
        @Schema(description = "密碼", example = "password123")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "註冊請求")
    public static class RegisterRequest {
        @NotBlank @Size(min = 3, max = 64)
        private String username;

        @NotBlank @Size(min = 6, max = 128)
        private String password;

        @NotBlank @Size(max = 64)
        private String realName;

        @NotBlank @Size(max = 20)
        private String phone;

        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "登入回應")
    public static class LoginResponse {
        @Schema(description = "Access Token")
        private String accessToken;

        @Schema(description = "Refresh Token")
        private String refreshToken;

        @Schema(description = "Token 類型")
        private String tokenType;

        @Schema(description = "過期時間（秒）")
        private long expiresIn;

        @Schema(description = "用戶資訊")
        private UserInfo userInfo;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UserInfo {
            private Long userId;
            private String username;
            private String realName;
            private String role;
            private Long clinicId;
        }
    }

    @Data
    @Schema(description = "Token 刷新請求")
    public static class RefreshRequest {
        @NotBlank
        private String refreshToken;
    }
}
