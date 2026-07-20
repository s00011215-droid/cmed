package com.xiangyun.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InventoryDTO {

    @Data @Schema(description = "庫存鎖定請求（處方開立時調用）")
    public static class LockRequest {
        @NotNull private Long prescriptionId;
        @NotEmpty private List<LockItem> items;
    }

    @Data public static class LockItem {
        @NotNull private Long materialId;
        @NotNull @DecimalMin("0.001") private BigDecimal qty;
    }

    @Data @Schema(description = "庫存鎖定結果")
    public static class LockResult {
        private boolean success;
        private Long prescriptionId;
        private List<String> insufficientMaterials;
    }

    @Data @Schema(description = "庫存解鎖請求（處方作廢時調用）")
    public static class UnlockRequest {
        @NotNull private Long prescriptionId;
    }

    @Data @Schema(description = "調劑核銷請求")
    public static class DispenseRequest {
        @NotNull private Long prescriptionId;
        private String pharmacistNote;
    }

    @Data @Schema(description = "庫存項目")
    public static class InventoryItem {
        private Long id; private Long materialId; private String materialName;
        private String batchNo; private LocalDate expireDate;
        private BigDecimal totalQty; private BigDecimal lockedQty;
        private BigDecimal availableQty;
        private BigDecimal unitPrice; private String location;
    }
}
