package com.xiangyun.decoction.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xiangyun.common.base.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Data @EqualsAndHashCode(callSuper = true)
@TableName(value = "decoction_order", autoResultMap = true)
public class DecoctionOrder extends BaseEntity {
    private Long prescriptionId;
    private String externalCenterId;
    private String status;       // pending/accepted/processing/packaged/ready/handed_over/cancelled
    private Integer doseCount;
    private BigDecimal fee;
    private String vacuumPkgNo;
    private String externalNo;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> craft;     // JSONB: 煎煮工藝

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> receiver;   // JSONB: 收件人

    private OffsetDateTime completedAt;
}
