package com.xiangyun.prescription.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyun.common.exception.BizException;
import com.xiangyun.prescription.dto.PrescriptionDTO;
import com.xiangyun.prescription.entity.IncompatibilityRule;
import com.xiangyun.prescription.entity.Prescription;
import com.xiangyun.prescription.entity.Prescription.PrescriptionItem;
import com.xiangyun.prescription.mapper.IncompatibilityMapper;
import com.xiangyun.prescription.mapper.PrescriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
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

    // ---- Query ----

    public Page<PrescriptionDTO.ListItem> listByPatient(Long patientId, int page, int size) {
        Page<Prescription> pg = prescriptionMapper.findByPatient(new Page<>(page, size), patientId);
        return (Page<PrescriptionDTO.ListItem>) pg.convert(p -> BeanUtil.copyProperties(p, PrescriptionDTO.ListItem.class));
    }

    public PrescriptionDTO.DetailResponse getDetail(Long id) {
        Prescription p = prescriptionMapper.selectById(id);
        if (p == null) throw new BizException(BizException.ErrorCode.PRESCRIPTION_NOT_FOUND);
        PrescriptionDTO.DetailResponse resp = BeanUtil.copyProperties(p, PrescriptionDTO.DetailResponse.class);
        resp.setWarnings(checkIncompatibility(p.getItems()));
        return resp;
    }

    // ---- Save (with auto-calculation + incompatibility check) ----

    @Transactional
    public PrescriptionDTO.DetailResponse save(PrescriptionDTO.SaveRequest req) {
        Prescription p = req.getId() != null ? prescriptionMapper.selectById(req.getId()) : new Prescription();
        if (p == null) throw new BizException(BizException.ErrorCode.PRESCRIPTION_NOT_FOUND);

        BeanUtil.copyProperties(req, p, "id","items");
        p.setItems(req.getItems());

        // 自動計算金額
        BigDecimal total = BigDecimal.ZERO;
        for (PrescriptionItem item : p.getItems()) {
            if (item.getUnitPrice() != null && item.getDosage() != null) {
                item.setSubtotal(item.getUnitPrice().multiply(item.getDosage()));
                total = total.add(item.getSubtotal());
            }
        }
        p.setTotalAmount(total.multiply(BigDecimal.valueOf(req.getDoseCount())));

        boolean isNew = req.getId() == null;
        if (isNew) {
            p.setStatus("draft");
            p.setPrescriptionNo("RX" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase());
            prescriptionMapper.insert(p);
        } else {
            if (!ACTIVE_STATUSES.contains(p.getStatus()))
                throw new BizException(BizException.ErrorCode.INVALID_STATUS_TRANSITION);
            prescriptionMapper.updateById(p);
        }

        PrescriptionDTO.DetailResponse resp = BeanUtil.copyProperties(p, PrescriptionDTO.DetailResponse.class);
        resp.setWarnings(checkIncompatibility(p.getItems()));
        return resp;
    }

    // ---- Status Machine ----

    @Transactional
    public void transition(Long id, PrescriptionDTO.StatusTransition req) {
        Prescription p = prescriptionMapper.selectById(id);
        if (p == null) throw new BizException(BizException.ErrorCode.PRESCRIPTION_NOT_FOUND);

        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(p.getStatus(), Set.of());
        if (!allowed.contains(req.getStatus())) {
            throw new BizException(422, "不允許從 " + p.getStatus() + " 轉換到 " + req.getStatus());
        }

        // 提交審核時強制配伍禁忌檢查 (block 級別)
        if ("pending_review".equals(req.getStatus()) || "approved".equals(req.getStatus())) {
            List<String> blocks = checkIncompatibilityBlockers(p.getItems());
            if (!blocks.isEmpty())
                throw new BizException(BizException.ErrorCode.INCOMPATIBILITY_DETECTED,
                        "配伍禁忌阻止: " + String.join(", ", blocks));
        }

        p.setStatus(req.getStatus());
        prescriptionMapper.updateById(p);
        log.info("Prescription {} status: {} -> {}", p.getPrescriptionNo(), p.getStatus(), req.getStatus());
    }

    // ---- Incompatibility Engine ----

    private List<String> checkIncompatibility(List<PrescriptionItem> items) {
        if (items == null || items.size() < 2) return List.of();
        List<Long> ids = items.stream().map(PrescriptionItem::getMaterialId).filter(Objects::nonNull).toList();
        if (ids.size() < 2) return List.of();
        return incompatibilityMapper.checkConflicts(ids).stream()
                .map(r -> "⚠ " + r.getDescription() + " [" + r.getSeverity() + "]")
                .toList();
    }

    private List<String> checkIncompatibilityBlockers(List<PrescriptionItem> items) {
        if (items == null || items.size() < 2) return List.of();
        List<Long> ids = items.stream().map(PrescriptionItem::getMaterialId).filter(Objects::nonNull).toList();
        if (ids.size() < 2) return List.of();
        return incompatibilityMapper.checkConflicts(ids).stream()
                .filter(r -> "block".equals(r.getSeverity()))
                .map(IncompatibilityRule::getDescription)
                .toList();
    }
}
