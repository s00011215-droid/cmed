package com.xiangyun.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data @TableName("inventory_transaction")
public class InventoryTransaction {
    private Long id; private Long clinicId; private Long inventoryId;
    private String type;    // purchase/lock/unlock/dispense/return/scrap
    private BigDecimal qty; private BigDecimal balanceAfter;
    private String referenceType; private Long referenceId;
    private String notes;
    private Long createdBy; private LocalDateTime createdAt;
}
