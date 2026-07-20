package com.xiangyun.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiangyun.patient.entity.FamilyMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {
    @Select("SELECT fm.*, p.name as member_name, p.phone as member_phone FROM family_member fm "
            + "JOIN patient p ON fm.member_id = p.id WHERE fm.patient_id = #{patientId} AND fm.deleted = false")
    List<FamilyMember> findByPatientId(Long patientId);
}
