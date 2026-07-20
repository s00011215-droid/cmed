package com.xiangyun.prescription.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyun.common.base.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("incompatibility_rule")
public class IncompatibilityRule extends BaseEntity {
    private Long materialA;
    private Long materialB;
    private String ruleType;     // eighteen_antagonism / nineteen_fear
    private String description;
    private String severity;     // warn / block
}
