package com.xiangyun.his.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiangyun.common.base.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data @EqualsAndHashCode(callSuper = true) @TableName("doctor_schedule")
public class DoctorSchedule extends BaseEntity {
    private Long doctorId;
    private LocalDate scheduleDate;
    private Integer timeSlot;      // 1=上午 2=下午 3=晚上
    private Integer maxPatients;
    private Integer bookedCount;
    private Integer status;        // 1=啟用
}
