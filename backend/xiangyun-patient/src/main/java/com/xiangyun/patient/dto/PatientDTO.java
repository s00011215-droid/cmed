package com.xiangyun.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

public class PatientDTO {
    @Data @Schema(description = "建立/更新患者")
    public static class SaveRequest {
        private Long id;
        @NotBlank private String name;
        private String gender; private String phone;
        private String idCardHash; private LocalDate birthDate;
        private String bloodType; private String allergyInfo;
        private String address; private String emergencyContact;
        private String tags;
    }
    @Data @Schema(description = "患者清單項") public static class ListItem {
        private Long id; private String name; private String gender;
        private String phone; private LocalDate birthDate; private String tags;
    }
    @Data @Schema(description = "家庭成員綁定") public static class FamilyBindRequest {
        @NotBlank private Long memberId;
        @NotBlank private String relation;
    }
    @Data @Schema(description = "患者詳情") public static class DetailResponse {
        private Long id; private String name; private String gender;
        private String phone; private LocalDate birthDate; private String bloodType;
        private String allergyInfo; private String address;
        private String emergencyContact; private String tags;
        private List<FamilyInfo> familyMembers;
    }
    @Data public static class FamilyInfo {
        private Long id; private Long memberId; private String relation;
        private String memberName; private String memberPhone;
    }
}
