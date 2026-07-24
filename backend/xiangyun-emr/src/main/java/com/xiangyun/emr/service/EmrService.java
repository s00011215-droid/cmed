package com.xiangyun.emr.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyun.common.audit.Auditable;
import com.xiangyun.common.exception.BizException;
import com.xiangyun.emr.dto.EmrDTO;
import com.xiangyun.emr.entity.Emr;
import com.xiangyun.emr.mapper.EmrMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class EmrService {
    private final EmrMapper emrMapper;

    public Page<EmrDTO.ListItem> listAll(int page, int size) {
        Page<Emr> pg = emrMapper.selectPage(new Page<>(page, size), null);
        return (Page<EmrDTO.ListItem>) pg.convert(e -> {
            EmrDTO.ListItem item = BeanUtil.copyProperties(e, EmrDTO.ListItem.class);
            item.setPatientName(""); item.setDoctorName("");
            return item;
        });
    }

    public Page<EmrDTO.ListItem> listByPatient(Long patientId, int page, int size) {
        Page<Emr> pg = emrMapper.findByPatient(new Page<>(page, size), patientId);
        return (Page<EmrDTO.ListItem>) pg.convert(e -> {
            EmrDTO.ListItem item = BeanUtil.copyProperties(e, EmrDTO.ListItem.class);
            item.setPatientName(""); item.setDoctorName("");
            return item;
        });
    }

    public Page<EmrDTO.ListItem> search(String keyword, int page, int size) {
        return (Page<EmrDTO.ListItem>) emrMapper.fullTextSearch(new Page<>(page, size), keyword)
                .convert(e -> BeanUtil.copyProperties(e, EmrDTO.ListItem.class));
    }

    @Auditable(action = "VIEW_SENSITIVE", target = "EMR")
    public EmrDTO.DetailResponse getDetail(Long id) {
        Emr e = emrMapper.selectById(id);
        if (e == null) throw new BizException(404, "病歷不存在");
        return BeanUtil.copyProperties(e, EmrDTO.DetailResponse.class);
    }

    @Transactional
    public Long save(EmrDTO.SaveRequest req) {
        Emr e = req.getId() != null ? emrMapper.selectById(req.getId()) : new Emr();
        if (e == null) throw new BizException(404, "病歷不存在");
        BeanUtil.copyProperties(req, e, "id");
        if (req.getId() != null) emrMapper.updateById(e);
        else emrMapper.insert(e);
        return e.getId();
    }
}
