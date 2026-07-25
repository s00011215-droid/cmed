package com.xiangyun.prescription.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xiangyun.common.base.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data @EqualsAndHashCode(callSuper = true) @TableName(value = "prescription", autoResultMap = true)
public class Prescription extends BaseEntity {
    private String prescriptionNo;
    private Long patientId; private Long doctorId; private Long emrId;
    private String visitType;
    private String status;
    private Integer doseCount;
    private Integer doseDays;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<PrescriptionItem> items;

    private BigDecimal totalAmount;
    private BigDecimal decoctionFee;
    private BigDecimal deliveryFee;
    private String decoctionMethod;
    private String deliveryOption;
    private String diagnosisCode;

    @TableField(exist = false) private String signStatus;
    @TableField(exist = false) private Long signedBy;
    @TableField(exist = false) private OffsetDateTime signedAt;

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PrescriptionItem {
        private Long materialId;
        @JsonProperty("material_name")
        private String materialName;
        private BigDecimal dosage;
        private String unit;
        private String processing;
        @JsonProperty("decoction_note")
        private String decoctionNote;
        @JsonProperty("unit_price")
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
