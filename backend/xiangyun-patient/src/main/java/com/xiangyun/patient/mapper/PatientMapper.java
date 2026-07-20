package com.xiangyun.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyun.patient.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

    @Select("<script>SELECT * FROM patient WHERE deleted=false "
            + "<if test='keyword!=null'>AND (name ILIKE CONCAT('%',#{keyword},'%') OR phone LIKE CONCAT('%',#{keyword},'%'))</if> "
            + "ORDER BY updated_at DESC</script>")
    Page<Patient> search(Page<Patient> page, @Param("keyword") String keyword);

    @Select("SELECT * FROM patient WHERE phone = #{phone} AND deleted = false LIMIT 1")
    Patient findByPhone(@Param("phone") String phone);
}
