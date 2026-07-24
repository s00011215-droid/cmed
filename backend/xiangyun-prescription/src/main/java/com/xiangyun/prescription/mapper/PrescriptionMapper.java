package com.xiangyun.prescription.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyun.prescription.entity.Prescription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {

    @Select("SELECT * FROM prescription WHERE patient_id = #{patientId} ORDER BY created_at DESC")
    Page<Prescription> findByPatient(Page<Prescription> page, @Param("patientId") Long patientId);

    @Select("SELECT * FROM prescription WHERE prescription_no = #{no} LIMIT 1")
    Prescription findByNo(@Param("no") String prescriptionNo);

    /** 取得進行中的處方（用於煎藥/物流狀態機） */
    @Select("SELECT * FROM prescription WHERE id = #{id} AND status IN ('paid','dispensing') LIMIT 1")
    Prescription findActiveById(@Param("id") Long id);
}
