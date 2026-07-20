package com.xiangyun.finance.service;

import cn.hutool.core.util.IdUtil;
import com.xiangyun.common.exception.BizException;
import com.xiangyun.finance.dto.FinanceDTO;
import com.xiangyun.finance.entity.Payment;
import com.xiangyun.finance.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j @Service @RequiredArgsConstructor
public class FinanceService {
    private final PaymentMapper paymentMapper;

    @Transactional
    public Payment createPayment(FinanceDTO.CreatePaymentRequest req) {
        Payment existing = paymentMapper.findByPrescription(req.getPrescriptionId());
        if (existing != null && "paid".equals(existing.getStatus()))
            throw new BizException(409, "該處方已支付");

        Payment p = new Payment();
        p.setPrescriptionId(req.getPrescriptionId());
        p.setAmount(req.getAmount());
        p.setMethod(req.getMethod());
        p.setStatus("paid");
        p.setPaidAt(LocalDateTime.now());
        p.setPaymentNo("PAY" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase());
        paymentMapper.insert(p);
        log.info("Payment created: {}, amount={}, method={}", p.getPaymentNo(), p.getAmount(), p.getMethod());
        return p;
    }

    @Transactional
    public Payment refund(FinanceDTO.RefundRequest req) {
        Payment p = paymentMapper.selectById(req.getPaymentId());
        if (p == null) throw new BizException(404, "支付記錄不存在");
        if (!"paid".equals(p.getStatus())) throw new BizException(409, "僅已支付訂單可退款");
        if (req.getAmount().compareTo(p.getAmount()) > 0)
            throw new BizException(400, "退款金額不可超過原支付金額");

        p.setStatus("refunded");
        p.setRefundAmount(req.getAmount());
        p.setRefundAt(LocalDateTime.now());
        paymentMapper.updateById(p);
        log.info("Refund processed: payment={}, amount={}, reason={}", p.getPaymentNo(), req.getAmount(), req.getReason());
        return p;
    }

    public FinanceDTO.DailySettlement getDailySettlement(LocalDate date) {
        Map<String, Object> summary = paymentMapper.dailySummary(date);
        FinanceDTO.DailySettlement s = new FinanceDTO.DailySettlement();
        s.setDate(date);
        s.setTotalRevenue((BigDecimal) summary.getOrDefault("revenue", BigDecimal.ZERO));
        s.setPrescriptionCount(((Long) summary.getOrDefault("count", 0L)).intValue());
        return s;
    }

    public Payment getByPrescription(Long prescriptionId) {
        return paymentMapper.findByPrescription(prescriptionId);
    }
}
