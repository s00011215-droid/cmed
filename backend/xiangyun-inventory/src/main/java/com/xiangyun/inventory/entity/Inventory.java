package com.xiangyun.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyun.common.base.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @EqualsAndHashCode(callSuper = true) @TableName("inventory")
public class Inventory extends BaseEntity {
    private Long materialId;
    private String batchNo;
    private String supplier;
    private LocalDate purchaseDate;
    private LocalDate expireDate;
    private BigDecimal totalQty;
    private BigDecimal lockedQty;    // 已鎖定（處方開立但未調劑）
    private BigDecimal unitCost;
    private BigDecimal unitPrice;
    private String location;
}
