package com.xiangyun.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyun.emr.entity.Emr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmrMapper extends BaseMapper<Emr> {

    @Select("SELECT * FROM emr WHERE patient_id = #{patientId} AND deleted = false ORDER BY created_at DESC")
    Page<Emr> findByPatient(Page<Emr> page, @Param("patientId") Long patientId);

    /** JSONB 全文搜尋：在 detail 欄位中搜關鍵字（利用 GIN 索引） */
    @Select("SELECT * FROM emr WHERE deleted = false AND "
            + "(detail::text ILIKE CONCAT('%',#{keyword},'%') "
            + " OR chief_complaint ILIKE CONCAT('%',#{keyword},'%') "
            + " OR diagnosis ILIKE CONCAT('%',#{keyword},'%')) "
            + "ORDER BY created_at DESC")
    Page<Emr> fullTextSearch(Page<Emr> page, @Param("keyword") String keyword);
}
