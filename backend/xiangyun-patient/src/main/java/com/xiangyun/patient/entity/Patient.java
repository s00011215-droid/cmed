package com.xiangyun.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyun.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data @EqualsAndHashCode(callSuper = true) @TableName("patient")
@Schema(description = "患者")
public class Patient extends BaseEntity {
    @Schema(description = "關聯線上帳號") private Long userId;
    @Schema(description = "姓名") private String name;
    @Schema(description = "性別") private String gender;
    @Schema(description = "手機號") private String phone;
    @Schema(description = "身分證哈希") private String idCardHash;
    @Schema(description = "出生日期") private LocalDate birthDate;
    @Schema(description = "血型") private String bloodType;
    @Schema(description = "過敏史 JSON") private String allergyInfo;
    @Schema(description = "地址") private String address;
    @Schema(description = "緊急聯絡人 JSON") private String emergencyContact;
    @Schema(description = "標籤") private String tags;
}
