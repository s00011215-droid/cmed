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
    private String prescriptionNo;       // 處方編號 (業務唯一)
    private Long patientId; private Long doctorId; private Long emrId;
    private String visitType;            // online / offline
    private String status;               // draft/pending_review/approved/paid/dispensing/completed/voided
    private Integer doseCount;           // 劑數
    private Integer doseDays;            // 服用天數

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<PrescriptionItem> items;  // JSONB: 處方明細

    private BigDecimal totalAmount;
    private BigDecimal decoctionFee;     // 代煎費
    private BigDecimal deliveryFee;      // 配送費
    private String decoctionMethod;      // self / center
    private String deliveryOption;       // pickup / delivery
    private String diagnosisCode;

    // 簽章欄位 (P0-2 修正)
    private String signStatus;           // unsigned/signed/verified
    private Long signedBy; private OffsetDateTime signedAt;
    private String contentHash;          // 原文 SHA-256
    private byte[] cmsSignature;         // PKCS#7 DER
    private String signAlgorithm;

    @Data
    public static class PrescriptionItem {
        private Long materialId;
        private String materialName;
        private BigDecimal dosage;
        private String unit;
        private String processing;       // 炮製方法
        private String decoctionNote;    // 先煎/後下/烊化/包煎/冲服/無
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
