package com.xiangyun.common.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 實體基底類別 — 所有資料庫實體的公共欄位
 * <p>
 * 包含審計欄位（created_by / updated_by / updated_at），
 * 配合 MyBatis-Plus 自動填充和審計 AOP 自動記錄。
 * </p>
 */
@Data
public abstract class BaseEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主鍵 ID (Snowflake)")
    private Long id;

    @Schema(description = "診所 ID（租戶）")
    private Long clinicId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "建立者 ID")
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "最後修改者 ID")
    private Long updatedBy;

    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "最後修改時間")
    private LocalDateTime updatedAt;

    /** 軟刪除標記（可選，子類按需使用） */
    @JsonIgnore
    @Schema(description = "是否刪除")
    private Boolean deleted;
}
