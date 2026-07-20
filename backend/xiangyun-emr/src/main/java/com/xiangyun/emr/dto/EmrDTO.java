package com.xiangyun.emr.dto;

import com.xiangyun.emr.entity.Emr.EmrDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EmrDTO {
    @Data @Schema(description = "建立/更新 EMR")
    public static class SaveRequest {
        private Long id;
        @NotNull private Long patientId;
        @NotNull private Long doctorId;
        @NotBlank private String visitType;
        private String chiefComplaint;
        private String presentIllness;
        private String pastHistory;
        private EmrDetail detail;
        private String diagnosis;
        private String advice;
        private LocalDate followUpDate;
    }
    @Data @Schema(description = "EMR 列表項") public static class ListItem {
        private Long id; private Long patientId; private String patientName;
        private Long doctorId; private String doctorName;
        private String visitType; private String chiefComplaint;
        private String diagnosis; private LocalDateTime createdAt;
    }
    @Data @Schema(description = "EMR 詳情") public static class DetailResponse {
        private Long id; private Long patientId; private String patientName;
        private Long doctorId; private String doctorName;
        private String visitType; private String chiefComplaint;
        private String presentIllness; private String pastHistory;
        private EmrDetail detail;
        private String diagnosis; private String advice;
        private LocalDate followUpDate;
        private LocalDateTime createdAt;
    }
}
