package com.xiangyun.patient.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyun.common.exception.BizException;
import com.xiangyun.patient.dto.PatientDTO;
import com.xiangyun.patient.entity.FamilyMember;
import com.xiangyun.patient.entity.Patient;
import com.xiangyun.patient.mapper.FamilyMemberMapper;
import com.xiangyun.patient.mapper.PatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class PatientService {
    private final PatientMapper patientMapper;
    private final FamilyMemberMapper familyMemberMapper;

    public Page<PatientDTO.ListItem> search(String keyword, int page, int size) {
        Page<Patient> pg = patientMapper.search(new Page<>(page, size), keyword);
        return (Page<PatientDTO.ListItem>) pg.convert(p -> BeanUtil.copyProperties(p, PatientDTO.ListItem.class));
    }

    public PatientDTO.DetailResponse getDetail(Long id) {
        Patient p = patientMapper.selectById(id);
        if (p == null) throw new BizException(BizException.ErrorCode.PATIENT_NOT_FOUND);
        PatientDTO.DetailResponse resp = BeanUtil.copyProperties(p, PatientDTO.DetailResponse.class);
        List<FamilyMember> members = familyMemberMapper.findByPatientId(id);
        resp.setFamilyMembers(members.stream().map(m -> {
            PatientDTO.FamilyInfo fi = new PatientDTO.FamilyInfo();
            fi.setId(m.getId()); fi.setMemberId(m.getMemberId()); fi.setRelation(m.getRelation());
            return fi;
        }).collect(Collectors.toList()));
        return resp;
    }

    @Transactional
    public Long save(PatientDTO.SaveRequest req) {
        Patient p = req.getId() != null ? patientMapper.selectById(req.getId()) : new Patient();
        if (p == null) throw new BizException(BizException.ErrorCode.PATIENT_NOT_FOUND);
        BeanUtil.copyProperties(req, p, "id");
        if (req.getId() != null) patientMapper.updateById(p);
        else patientMapper.insert(p);
        return p.getId();
    }

    public void delete(Long id) {
        Patient p = patientMapper.selectById(id);
        if (p == null) throw new BizException(BizException.ErrorCode.PATIENT_NOT_FOUND);
        p.setDeleted(true);
        patientMapper.updateById(p);
    }

    @Transactional
    public void bindFamily(Long patientId, PatientDTO.FamilyBindRequest req) {
        FamilyMember fm = new FamilyMember();
        fm.setPatientId(patientId); fm.setMemberId(req.getMemberId()); fm.setRelation(req.getRelation());
        familyMemberMapper.insert(fm);
    }

    public void unbindFamily(Long patientId, Long memberId) {
        familyMemberMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getPatientId, patientId).eq(FamilyMember::getMemberId, memberId));
    }
}
