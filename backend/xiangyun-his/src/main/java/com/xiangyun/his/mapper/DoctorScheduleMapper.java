package com.xiangyun.his.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiangyun.his.entity.DoctorSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;

@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {
    @Select("SELECT * FROM doctor_schedule WHERE doctor_id = #{doctorId} "
            + "AND schedule_date = #{date} AND time_slot = #{timeSlot} AND status = 1 LIMIT 1")
    DoctorSchedule findByDoctorAndDate(@Param("doctorId") Long doctorId,
            @Param("date") LocalDate date, @Param("timeSlot") Integer timeSlot);
}
