package com.xiangyun.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyun.common.base.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data @EqualsAndHashCode(callSuper = true) @TableName("payment")
public class Payment extends BaseEntity {
    private Long prescriptionId;
    private String paymentNo;
    private BigDecimal amount;
    private String method;      // cash/octopus/alipay/wechat/credit_card/insurance
    private String status;      // pending/paid/refunding/refunded/failed
    private String transactionId;
    private OffsetDateTime paidAt;
    private BigDecimal refundAmount;
    private OffsetDateTime refundAt;
}
