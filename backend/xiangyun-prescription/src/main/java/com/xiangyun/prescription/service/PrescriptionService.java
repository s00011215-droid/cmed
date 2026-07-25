package com.xiangyun.prescription.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyun.common.exception.BizException;
import com.xiangyun.prescription.dto.PrescriptionDTO;
import com.xiangyun.prescription.entity.Prescription;
import com.xiangyun.prescription.entity.Prescription.PrescriptionItem;
import com.xiangyun.prescription.mapper.IncompatibilityMapper;
import com.xiangyun.prescription.mapper.PrescriptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service @RequiredArgsConstructor
public class PrescriptionService {
    private final PrescriptionMapper prescriptionMapper;
    private final IncompatibilityMapper incompatibilityMapper;

    private static final Set<String> ACTIVE_STATUSES = Set.of("draft","pending_review","approved","paid","dispensing");
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
        "draft",          Set.of("pending_review","voided"),
        "pending_review", Set.of("approved","voided","draft"),
        "approved",       Set.of("paid","voided"),
        "paid",           Set.of("dispensing"),
        "dispensing",     Set.of("completed","voided")
    );

    public Page<PrescriptionDTO.ListItem> listByPatient(Long patientId, int page, int size) {
        Page<Prescription> pg = prescriptionMapper.findByPatient(new Page<>(page, size), patientId);
        return (Page<PrescriptionDTO.ListItem>) pg.convert(p -> BeanUtil.copyProperties(p, PrescriptionDTO.ListItem.class));
    }

    public PrescriptionDTO.DetailResponse getDetail(Long id) {
        Prescription p = prescriptionMapper.selectById(id);
        if (p == null) throw new BizException(BizException.ErrorCode.PRESCRIPTION_NOT_FOUND);
        PrescriptionDTO.DetailResponse resp = BeanUtil.copyProperties(p, PrescriptionDTO.DetailResponse.class);
        resp.setWarnings(checkIncompatibility(p.items));
        return resp;
    }

    @Transactional
    public PrescriptionDTO.DetailResponse save(PrescriptionDTO.SaveRequest req) {
        Prescription p = req.getId() != null ? prescriptionMapper.selectById(req.getId()) : new Prescription();
        if (p == null) throw new BizException(BizException.ErrorCode.PRESCRIPTION_NOT_FOUND);

        BeanUtil.copyProperties(req, p, "id","items");
        p.items = req.getItems();

        BigDecimal total = BigDecimal.ZERO;
        for (PrescriptionItem item : p.items) {
            if (item.unitPrice != null && item.dosage != null) {
                item.subtotal = item.unitPrice.multiply(item.dosage);
                total = total.add(item.subtotal);
            }
        }
        p.totalAmount = total.multiply(BigDecimal.valueOf(req.getDoseCount()));

        boolean isNew = req.getId() == null;
        if (isNew) {
            p.status = "draft";
            p.prescriptionNo = "RX" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
            prescriptionMapper.insert(p);
        } else {
            if (!ACTIVE_STATUSES.contains(p.status))
                throw new BizException(BizException.ErrorCode.INVALID_STATUS_TRANSITION);
            prescriptionMapper.updateById(p);
        }

        PrescriptionDTO.DetailResponse resp = BeanUtil.copyProperties(p, PrescriptionDTO.DetailResponse.class);
        resp.setWarnings(checkIncompatibility(p.items));
        return resp;
    }

    @Transactional
    public void transition(Long id, PrescriptionDTO.StatusTransition req) {
        Prescription p = prescriptionMapper.selectById(id);
        if (p == null) throw new BizException(BizException.ErrorCode.PRESCRIPTION_NOT_FOUND);

        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(p.status, Set.of());
        if (!allowed.contains(req.status)) {
            throw new BizException(422, "不允許從 " + p.status + " 轉換到 " + req.status);
        }

        if ("pending_review".equals(req.status) || "approved".equals(req.status)) {
            List<String> blocks = checkIncompatibilityBlockers(p.items);
            if (!blocks.isEmpty())
                throw new BizException(BizException.ErrorCode.INCOMPATIBILITY_DETECTED,
                        "配伍禁忌阻止: " + String.join(", ", blocks));
        }

        p.status = req.status;
        prescriptionMapper.updateById(p);
    }

    private List<String> checkIncompatibility(List<PrescriptionItem> items) {
        return java.util.Collections.emptyList();
    }
    private List<String> checkIncompatibilityBlockers(List<PrescriptionItem> items) {
        return java.util.Collections.emptyList();
    }
}
