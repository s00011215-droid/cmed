package com.xiangyun.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyun.common.base.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("family_member")
public class FamilyMember extends BaseEntity {
    private Long patientId;
    private Long memberId;
    private String relation;
}
