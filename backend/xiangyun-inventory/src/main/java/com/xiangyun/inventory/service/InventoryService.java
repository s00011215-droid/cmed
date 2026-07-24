package com.xiangyun.inventory.service;

import com.xiangyun.common.exception.BizException;
import com.xiangyun.inventory.dto.InventoryDTO;
import com.xiangyun.inventory.entity.Inventory;
import com.xiangyun.inventory.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j @Service @RequiredArgsConstructor
public class InventoryService {
    private final InventoryMapper inventoryMapper;
    private final RedissonClient redissonClient;

    /** 鎖定庫存（處方支付成功後） */
    @Transactional
    public InventoryDTO.LockResult lock(InventoryDTO.LockRequest req) {
        InventoryDTO.LockResult result = new InventoryDTO.LockResult();
        result.setPrescriptionId(req.getPrescriptionId());
        result.setSuccess(true);
        List<String> insufficient = new ArrayList<>();

        // 按 material_id 分組匯總需求量
        Map<Long, BigDecimal> demand = new HashMap<>();
        for (var item : req.getItems())
            demand.merge(item.getMaterialId(), item.getQty(), BigDecimal::add);

        for (var entry : demand.entrySet()) {
            Long materialId = entry.getKey();
            BigDecimal needed = entry.getValue();
            RLock lock = redissonClient.getLock("inv:lock:" + materialId);
            try {
                if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                    result.setSuccess(false);
                    insufficient.add("material " + materialId + ": 獲取鎖超時");
                    continue;
                }
                // FIFO 效期優先扣庫
                List<Inventory> available = inventoryMapper.findAvailable(materialId);
                BigDecimal remaining = needed;
                for (Inventory inv : available) {
                    if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
                    BigDecimal canLock = inv.getTotalQty().subtract(inv.getLockedQty());
                    BigDecimal toLock = canLock.min(remaining);
                    int rows = inventoryMapper.lockStock(inv.getId(), toLock);
                    if (rows > 0) remaining = remaining.subtract(toLock);
                }
                if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                    result.setSuccess(false);
                    insufficient.add("material " + materialId + ": 庫存不足 (缺 " + remaining + ")");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                result.setSuccess(false);
            } finally {
                if (lock.isHeldByCurrentThread()) lock.unlock();
            }
        }

        result.setInsufficientMaterials(insufficient);
        if (!result.isSuccess()) {
            // 鎖定失敗 → 回滾已鎖部分
            unlock(new InventoryDTO.UnlockRequest());
        }
        return result;
    }

    /** 解鎖庫存（處方作廢/鎖定失敗回滾） */
    @Transactional
    public void unlock(InventoryDTO.UnlockRequest req) {
        // 實際應從 inventory_transaction 查出該處方鎖定的記錄，逐筆解鎖
        log.info("Unlocking inventory for prescription: {}", req.getPrescriptionId());
    }

    /** 調劑核銷（藥房出藥） */
    @Transactional
    public void dispense(InventoryDTO.DispenseRequest req) {
        log.info("Dispensing prescription: {}", req.getPrescriptionId());
        // 從 inventory_transaction 找出鎖定記錄 → dispense() → 扣減 total_qty + locked_qty
    }

    /** 效期預警 */
    public List<Inventory> getExpiringSoon() {
        return inventoryMapper.findExpiringSoon();
    }

    /** 搜尋庫存 */
    public List<Inventory> findAvailable(Long materialId) {
        return inventoryMapper.findAvailable(materialId);
    }
}
