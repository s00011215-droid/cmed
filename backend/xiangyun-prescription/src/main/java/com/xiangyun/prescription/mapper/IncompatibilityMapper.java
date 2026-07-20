package com.xiangyun.prescription.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiangyun.prescription.entity.IncompatibilityRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface IncompatibilityMapper extends BaseMapper<IncompatibilityRule> {
    /** 檢查給定藥材 ID 集合中是否有配伍禁忌 */
    @Select("<script>SELECT * FROM incompatibility_rule WHERE "
            + "material_a IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "AND material_b IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "AND material_a &lt; material_b</script>")
    List<IncompatibilityRule> checkConflicts(@Param("ids") List<Long> materialIds);
}
