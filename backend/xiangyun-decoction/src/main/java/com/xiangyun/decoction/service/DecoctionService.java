package com.xiangyun.decoction.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.xiangyun.common.exception.BizException;
import com.xiangyun.decoction.dto.DecoctionDTO;
import com.xiangyun.decoction.entity.DecoctionOrder;
import com.xiangyun.decoction.mapper.DecoctionOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j @Service @RequiredArgsConstructor
public class DecoctionService {
    private final DecoctionOrderMapper decoctionOrderMapper;
    private final StringRedisTemplate redisTemplate;

    @Value("${decoction.hmac-key:xiangyun-decoction-hmac-key}")
    private String hmacKey;

    private static final Set<String> ACTIVE_STATUSES =
            Set.of("pending","accepted","processing","packaged","ready");
    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
        "pending",    Set.of("accepted","cancelled"),
        "accepted",   Set.of("processing","cancelled"),
        "processing", Set.of("packaged"),
        "packaged",   Set.of("ready"),
        "ready",      Set.of("handed_over")
    );

    /** 下發煎藥訂單 */
    @Transactional
    public DecoctionOrder create(DecoctionDTO.CreateRequest req) {
        // 檢查是否有進行中的煎藥單
        Long existing = decoctionOrderMapper.findActiveByPrescription(req.getPrescriptionId());
        if (existing != null) throw new BizException(BizException.ErrorCode.DECOCTION_ORDER_EXISTS);

        DecoctionOrder order = new DecoctionOrder();
        order.setPrescriptionId(req.getPrescriptionId());
        order.setDoseCount(req.getDoseCount());
        order.setFee(BigDecimal.valueOf(req.getDoseCount() * 30)); // 預設代煎費
        order.setCraft(req.getCraft());
        order.setReceiver(req.getReceiver());
        order.setStatus("pending");
        order.setExternalNo("DEC-" + IdUtil.fastSimpleUUID().substring(0, 10).toUpperCase());
        decoctionOrderMapper.insert(order);

        // 透過 MQ/HTTP 推送給煎藥中心（此處僅記錄）
        log.info("Decoction order created: {}, prescription={}, calling external center...",
                order.getExternalNo(), req.getPrescriptionId());

        return order;
    }

    /** 取消煎藥訂單（僅 pending/accepted 狀態可取消） */
    @Transactional
    public void cancel(Long orderId) {
        DecoctionOrder order = decoctionOrderMapper.selectById(orderId);
        if (order == null) throw new BizException(404, "煎藥訂單不存在");
        if (!Set.of("pending","accepted").contains(order.getStatus()))
            throw new BizException(BizException.ErrorCode.DECOCTION_CANCEL_FAILED);
        order.setStatus("cancelled");
        decoctionOrderMapper.updateById(order);
    }

    /** 處理煎藥中心回調（含 HMAC 驗證 + 冪等保護） */
    @Transactional
    public void handleCallback(DecoctionDTO.StatusCallback callback, String signature) {
        // 1. HMAC 簽名驗證
        String payload = callback.getExternalNo() + callback.getStatus() + callback.getTimestamp();
        HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, hmacKey.getBytes());
        String expected = hmac.digestHex(payload);
        if (!expected.equals(signature)) {
            log.warn("HMAC verification failed for callback: externalNo={}", callback.getExternalNo());
            throw new BizException(BizException.ErrorCode.TOKEN_INVALID); // reuse 401 code
        }

        // 2. 冪等保護
        String idempotentKey = "dc:cb:" + callback.getExternalNo() + ":" + callback.getStatus();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", Duration.ofHours(24));
        if (Boolean.FALSE.equals(locked)) {
            log.info("Duplicate callback ignored: {}", idempotentKey);
            return; // 冪等返回
        }

        // 3. 查找訂單
        DecoctionOrder order = decoctionOrderMapper.findByExternalNo(callback.getExternalNo());
        if (order == null) throw new BizException(404, "煎藥訂單不存在: " + callback.getExternalNo());

        // 4. 狀態機校驗
        Set<String> allowed = VALID_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(callback.getStatus()))
            throw new BizException(BizException.ErrorCode.INVALID_STATUS_TRANSITION);

        // 5. 更新狀態
        order.setStatus(callback.getStatus());
        if ("packaged".equals(callback.getStatus())) order.setVacuumPkgNo(callback.getVacuumPkgNo());
        if ("handed_over".equals(callback.getStatus())) order.setCompletedAt(LocalDateTime.now());
        decoctionOrderMapper.updateById(order);

        log.info("Decoction callback processed: {} -> {}", callback.getExternalNo(), callback.getStatus());
    }

    public DecoctionOrder getById(Long id) {
        DecoctionOrder o = decoctionOrderMapper.selectById(id);
        if (o == null) throw new BizException(404, "煎藥訂單不存在");
        return o;
    }
}
