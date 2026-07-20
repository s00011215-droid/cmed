package com.xiangyun.decoction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiangyun.decoction.entity.DecoctionOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface DecoctionOrderMapper extends BaseMapper<DecoctionOrder> {

    @Select("SELECT id FROM decoction_order WHERE prescription_id = #{prescriptionId} "
            + "AND status IN ('pending','accepted','processing','packaged','ready') LIMIT 1")
    Long findActiveByPrescription(@Param("prescriptionId") Long prescriptionId);

    @Select("SELECT * FROM decoction_order WHERE external_no = #{externalNo} LIMIT 1")
    DecoctionOrder findByExternalNo(@Param("externalNo") String externalNo);
}
