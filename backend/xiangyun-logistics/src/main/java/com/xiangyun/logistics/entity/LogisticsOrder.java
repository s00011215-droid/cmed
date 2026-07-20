package com.xiangyun.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xiangyun.common.base.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.Map;

@Data @EqualsAndHashCode(callSuper = true)
@TableName(value = "logistics_order", autoResultMap = true)
public class LogisticsOrder extends BaseEntity {
    private Long decoctionId;
    private String carrier;
    private String carrierName;
    private String waybillNo;
    private String electronicSheet;
    private String status;       // created/picked_up/in_transit/delivering/signed/exception

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> receiver;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> latestTrace;

    private LocalDateTime estimatedDelivery;
    private LocalDateTime signedAt;
    private String signedBy;
    private String exceptionReason;
}
