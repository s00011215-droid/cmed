package com.xiangyun.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiangyun.inventory.entity.Inventory;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Select("SELECT * FROM inventory WHERE material_id = #{materialId} AND total_qty - locked_qty > 0 AND expire_date > CURRENT_DATE ORDER BY expire_date ASC")
    List<Inventory> findAvailable(@Param("materialId") Long materialId);

    /** 原子鎖定庫存 (樂觀鎖，由 Service 層 Redis 分布式鎖保護) */
    @Update("UPDATE inventory SET locked_qty = locked_qty + #{qty} "
            + "WHERE id = #{id} AND total_qty - locked_qty >= #{qty}")
    int lockStock(@Param("id") Long id, @Param("qty") BigDecimal qty);

    @Update("UPDATE inventory SET locked_qty = locked_qty - #{qty}, total_qty = total_qty - #{qty} "
            + "WHERE id = #{id} AND locked_qty >= #{qty} AND total_qty >= #{qty}")
    int dispense(@Param("id") Long id, @Param("qty") BigDecimal qty);

    @Update("UPDATE inventory SET locked_qty = locked_qty - #{qty} "
            + "WHERE id = #{id} AND locked_qty >= #{qty}")
    int unlock(@Param("id") Long id, @Param("qty") BigDecimal qty);

    @Select("SELECT * FROM inventory WHERE expire_date <= CURRENT_DATE + INTERVAL '30 days' AND total_qty - locked_qty > 0 ORDER BY expire_date")
    List<Inventory> findExpiringSoon();
}
