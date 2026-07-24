package com.xiangyun.account.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyun.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

/**
 * 用戶帳號實體
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_account")
@Schema(description = "用戶帳號")
public class UserAccount extends BaseEntity {

    @Schema(description = "用戶名")
    private String username;

    @Schema(description = "密碼哈希 (bcrypt)")
    private String passwordHash;

    @Schema(description = "真實姓名")
    private String realName;

    @Schema(description = "手機號")
    private String phone;

    @Schema(description = "電子郵件")
    private String email;

    @Schema(description = "角色: patient, doctor, nurse, pharmacist, admin, super_admin")
    private String role;

    @Schema(description = "狀態: 1=啟用 0=停用 2=待驗證")
    private Integer status;

    @Schema(description = "最後登入時間")
    private OffsetDateTime lastLoginAt;
}
