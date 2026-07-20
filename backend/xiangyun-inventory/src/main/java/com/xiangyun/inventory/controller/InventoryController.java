package com.xiangyun.inventory.controller;

import com.xiangyun.common.result.Result;
import com.xiangyun.inventory.dto.InventoryDTO;
import com.xiangyun.inventory.entity.Inventory;
import com.xiangyun.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/inventory") @RequiredArgsConstructor
@Tag(name = "庫存管理", description = "庫存鎖定/解鎖/調劑核銷/效期盤點")
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/lock")
    @Operation(summary = "鎖定庫存（處方開立/支付時）")
    public Result<InventoryDTO.LockResult> lock(@Valid @RequestBody InventoryDTO.LockRequest req) {
        return Result.ok(inventoryService.lock(req));
    }

    @PostMapping("/unlock")
    @Operation(summary = "解鎖庫存（處方作廢時）")
    public Result<Void> unlock(@Valid @RequestBody InventoryDTO.UnlockRequest req) {
        inventoryService.unlock(req);
        return Result.ok();
    }

    @PostMapping("/dispense")
    @Operation(summary = "調劑核銷（藥房出藥）")
    public Result<Void> dispense(@Valid @RequestBody InventoryDTO.DispenseRequest req) {
        inventoryService.dispense(req);
        return Result.ok();
    }

    @GetMapping("/available/{materialId}")
    @Operation(summary = "查詢可用庫存")
    public Result<List<Inventory>> findAvailable(@PathVariable Long materialId) {
        return Result.ok(inventoryService.findAvailable(materialId));
    }

    @GetMapping("/expiring")
    @Operation(summary = "效期預警（30天內到期）")
    public Result<List<Inventory>> getExpiringSoon() {
        return Result.ok(inventoryService.getExpiringSoon());
    }
}
