package com.xiangyun.logistics.service;

import cn.hutool.core.util.IdUtil;
import com.xiangyun.common.exception.BizException;
import com.xiangyun.logistics.dto.LogisticsDTO;
import com.xiangyun.logistics.entity.LogisticsOrder;
import com.xiangyun.logistics.mapper.LogisticsOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j @Service @RequiredArgsConstructor
public class LogisticsService {
    private final LogisticsOrderMapper logisticsOrderMapper;
    private final StringRedisTemplate redisTemplate;

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
        "created",     Set.of("picked_up"),
        "picked_up",   Set.of("in_transit"),
        "in_transit",  Set.of("delivering"),
        "delivering",  Set.of("signed","exception"),
        "exception",   Set.of("delivering","signed")
    );

    /** 自動打單（煎藥完成後） */
    @Transactional
    public LogisticsOrder create(LogisticsDTO.CreateRequest req) {
        LogisticsOrder order = new LogisticsOrder();
        order.setDecoctionId(req.getDecoctionId());
        order.setCarrier(req.getCarrier() != null ? req.getCarrier() : "sf");
        order.setCarrierName("sf".equals(order.getCarrier()) ? "順豐速運" : order.getCarrier());
        order.setReceiver(req.getReceiver());
        order.setStatus("created");
        order.setWaybillNo("SF" + IdUtil.fastSimpleUUID().substring(0, 12).toUpperCase());
        logisticsOrderMapper.insert(order);
        log.info("Logistics order created: waybill={}, carrier={}", order.getWaybillNo(), order.getCarrierName());
        return order;
    }

    /** 處理物流軌跡回調（含冪等） */
    @Transactional
    public void handleTraceCallback(LogisticsDTO.TraceCallback callback) {
        String idempotentKey = "log:cb:" + callback.getWaybillNo() + ":" + callback.getStatus();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", Duration.ofHours(12));
        if (Boolean.FALSE.equals(locked)) return;

        LogisticsOrder order = logisticsOrderMapper.findByWaybill(callback.getWaybillNo());
        if (order == null) throw new BizException(404, "物流單不存在: " + callback.getWaybillNo());

        Set<String> allowed = VALID_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(callback.getStatus()))
            throw new BizException(BizException.ErrorCode.INVALID_STATUS_TRANSITION);

        order.setStatus(callback.getStatus());
        if (callback.getTraces() != null && !callback.getTraces().isEmpty())
            order.setLatestTrace(callback.getTraces().get(callback.getTraces().size() - 1));
        if ("signed".equals(callback.getStatus())) {
            order.setSignedAt(OffsetDateTime.now());
            order.setSignedBy(callback.getSignedBy());
        }
        if ("exception".equals(callback.getStatus()))
            order.setExceptionReason(callback.getExceptionReason());
        logisticsOrderMapper.updateById(order);

        log.info("Logistics trace callback: {} -> {}", callback.getWaybillNo(), callback.getStatus());
    }

    /** 主動查詢軌跡（兜底） */
    public LogisticsOrder getTrace(String waybillNo) {
        LogisticsOrder o = logisticsOrderMapper.findByWaybill(waybillNo);
        if (o == null) throw new BizException(404, "物流單不存在");
        return o;
    }
}
