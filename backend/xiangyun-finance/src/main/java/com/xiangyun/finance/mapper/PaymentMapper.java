package com.xiangyun.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiangyun.finance.entity.Payment;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("SELECT * FROM payment WHERE prescription_id = #{prescriptionId} ORDER BY created_at DESC LIMIT 1")
    Payment findByPrescription(@Param("prescriptionId") Long prescriptionId);

    /** Daily revenue summary */
    @Select("SELECT COALESCE(SUM(amount),0) as revenue, COUNT(*) as count "
            + "FROM payment WHERE status = 'paid' AND DATE(paid_at) = #{date}")
    Map<String, Object> dailySummary(@Param("date") LocalDate date);
}
