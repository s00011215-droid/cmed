package com.xiangyun.emr.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xiangyun.common.base.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Data @EqualsAndHashCode(callSuper = true) @TableName(value = "emr", autoResultMap = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Emr extends BaseEntity {
    private Long patientId; private Long doctorId;
    private String visitType;       // online / offline
    private String chiefComplaint;  // 主訴
    private String presentIllness;  // 現病史
    private String pastHistory;     // 既往史

    @TableField(typeHandler = JacksonTypeHandler.class)
    private EmrDetail detail;       // JSONB: 望聞問切

    private String advice;          // 醫囑
    private java.time.LocalDate followUpDate;
    private String diagnosis;       // 診斷 (逗號分隔或陣列)

    // ---- JSONB 內部結構 ----
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EmrDetail {
        private Inspection inspection;
        private Auscultation auscultation;
        private Inquiry inquiry;
        private Palpation palpation;
        private String tcmPattern;          // 辨證
        private String treatmentPrinciple;  // 治則
        private Double temperature;
        private String bloodPressure;
        private Integer heartRate;
    }
    @Data public static class Inspection {
        private String tongue;  // 舌象
        private String face;    // 面色
        private String spirit;  // 神態
    }
    @Data public static class Auscultation {
        private String voice; private String cough; private String breath;
    }
    @Data public static class Inquiry {
        private String appetite; private String sleep;
        private String stool; private String urine;
    }
    @Data public static class Palpation {
        private String pulseLeft; private String pulseRight;
        private String pulseDetail;
    }
}
