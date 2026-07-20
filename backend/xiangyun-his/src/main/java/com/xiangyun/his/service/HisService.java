package com.xiangyun.his.service;

import com.xiangyun.common.exception.BizException;
import com.xiangyun.his.dto.HisDTO;
import com.xiangyun.his.entity.DoctorSchedule;
import com.xiangyun.his.mapper.DoctorScheduleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j @Service @RequiredArgsConstructor
public class HisService {
    private final DoctorScheduleMapper scheduleMapper;

    // 簡化版：內存隊列（生產環境應換 Redis Sorted Set）
    private final Map<Long, List<HisDTO.QueueItem>> queues = new ConcurrentHashMap<>();

    @Transactional
    public HisDTO.Registration register(HisDTO.RegistrationRequest req) {
        // 檢查排班
        DoctorSchedule schedule = findOrCreateSchedule(req);
        if (schedule.getBookedCount() >= schedule.getMaxPatients())
            throw new BizException(BizException.ErrorCode.SCHEDULE_FULL);

        // 更新預約數
        schedule.setBookedCount(schedule.getBookedCount() + 1);
        scheduleMapper.updateById(schedule);

        // 加入隊列
        HisDTO.QueueItem item = new HisDTO.QueueItem();
        item.setPatientId(req.getPatientId());
        item.setStatus("waiting");
        item.setQueueNumber(schedule.getBookedCount());

        queues.computeIfAbsent(req.getDoctorId(), k -> new ArrayList<>()).add(item);

        HisDTO.Registration reg = new HisDTO.Registration();
        reg.setId(schedule.getId());
        reg.setPatientId(req.getPatientId());
        reg.setDoctorId(req.getDoctorId());
        reg.setDate(req.getDate());
        reg.setTimeSlot(req.getTimeSlot());
        reg.setStatus("registered");
        reg.setRegistrationFee(BigDecimal.valueOf(100)); // 預設掛號費
        return reg;
    }

    public List<HisDTO.QueueItem> getQueue(Long doctorId) {
        return queues.getOrDefault(doctorId, List.of());
    }

    @Transactional
    public HisDTO.BillingRequest bill(HisDTO.BillingRequest req) {
        // 實際應調用 Prescription Service 獲取處方金額並建立 Payment 記錄
        log.info("Billing: prescriptionId={}, method={}, amount={}",
                req.getPrescriptionId(), req.getPaymentMethod(), req.getAmount());
        return req;
    }

    @Transactional
    public HisDTO.DispenseRequest dispense(HisDTO.DispenseRequest req) {
        // 實際應更新處方狀態為 dispensing/completed，並核銷庫存
        log.info("Dispensing: prescriptionId={}, note={}",
                req.getPrescriptionId(), req.getPharmacistNote());
        return req;
    }

    private DoctorSchedule findOrCreateSchedule(HisDTO.RegistrationRequest req) {
        DoctorSchedule schedule = scheduleMapper.findByDoctorAndDate(
                req.getDoctorId(), req.getDate(), req.getTimeSlot());
        if (schedule != null) return schedule;

        // 自動建立排班（佔位邏輯，實際應由醫生後台設定）
        schedule = new DoctorSchedule();
        schedule.setDoctorId(req.getDoctorId());
        schedule.setScheduleDate(req.getDate());
        schedule.setTimeSlot(req.getTimeSlot());
        schedule.setMaxPatients(20);
        schedule.setBookedCount(0);
        schedule.setStatus(1);
        scheduleMapper.insert(schedule);
        return schedule;
    }
}
