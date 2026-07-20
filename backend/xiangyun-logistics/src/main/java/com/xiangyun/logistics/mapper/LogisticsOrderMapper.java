package com.xiangyun.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiangyun.logistics.entity.LogisticsOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LogisticsOrderMapper extends BaseMapper<LogisticsOrder> {
    @Select("SELECT * FROM logistics_order WHERE waybill_no = #{waybillNo} LIMIT 1")
    LogisticsOrder findByWaybill(@Param("waybillNo") String waybillNo);
}
