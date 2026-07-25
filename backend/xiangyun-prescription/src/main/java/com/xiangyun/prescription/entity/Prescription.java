package com.xiangyun.prescription.entity;

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
    public String prescriptionNo;
    public Long patientId; public Long doctorId; public Long emrId;
    public String visitType;
    public String status;
    public Integer doseCount;
    public BigDecimal totalAmount;
    public List<PrescriptionItem> items;
    public BigDecimal decoctionFee;
    public BigDecimal deliveryFee;
    public String decoctionMethod;
    public String deliveryOption;
    public String diagnosisCode;

    @Data
    public static class PrescriptionItem {
        public Long materialId;
        public String materialName;
        public BigDecimal dosage;
        public String unit;
        public String processing;
        public String decoctionNote;
        public BigDecimal unitPrice;
        public BigDecimal subtotal;
    }
}
