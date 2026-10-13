package com.autonomous.bus.service.impl;

import com.autonomous.bus.entity.Bus;
import com.autonomous.bus.entity.BusSchedule;
import com.autonomous.bus.entity.Route;
import com.autonomous.bus.entity.TripRecord;
import com.autonomous.bus.mapper.TripRecordMapper;
import com.autonomous.bus.service.BusService;
import com.autonomous.bus.service.RouteService;
import com.autonomous.bus.service.ScheduleService;
import com.autonomous.bus.service.TripService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TripServiceImpl extends ServiceImpl<TripRecordMapper, TripRecord> implements TripService {

    /** 车辆状态：1在线(待命) 2离线 3检修 4运营中 */
    private static final int BUS_READY = 1;
    private static final int BUS_RUNNING = 4;

    /** 排班状态：0待发车 1待派车 2运行中 3已完成 4已停运 5已取消 */
    private static final int SCHED_PENDING = 0;
    private static final int SCHED_WAIT_DISPATCH = 1;
    private static final int SCHED_RUNNING = 2;
    private static final int SCHED_DONE = 3;

    /** 车次状态：0待发车 1运行中 2已完成 3已中断 */
    private static final int TRIP_RUNNING = 1;
    private static final int TRIP_DONE = 2;
    private static final int TRIP_INTERRUPT = 3;

    @Resource
    private BusService busService;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private RouteService routeService;

    /**
     * 查找可派车的车辆：
     * 状态在线待命(1)或运营中(4)、电量充足(>=20)、且归属指定线路（严格匹配，不可用未分配车辆调配）
     */
    private Bus findReadyBus(Long routeId) {
        LambdaQueryWrapper<Bus> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Bus::getStatus, BUS_READY, BUS_RUNNING);
        // 电量充足：battery >= 20 或 battery为空视为充足
        wrapper.and(w -> w.isNull(Bus::getBattery).or().ge(Bus::getBattery, 20));
        // 车辆必须归属该线路
        wrapper.eq(Bus::getRouteId, routeId);
        wrapper.orderByAsc(Bus::getRouteId).last("limit 1");
        List<Bus> list = busService.list(wrapper);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 校验车辆是否处于可派遣状态（在线待命1 或 运营中4） */
    private boolean isDispatchable(Bus bus) {
        return bus.getStatus() != null
                && (bus.getStatus() == BUS_READY || bus.getStatus() == BUS_RUNNING);
    }

    /** 把车辆改为待命(1) */
    private void setBusReady(Long vehicleId) {
        Bus bus = new Bus();
        bus.setId(vehicleId);
        bus.setStatus(BUS_READY);
        busService.updateById(bus);
    }

    /** 把车辆改为运营中(4) */
    private void setBusRunning(Long vehicleId) {
        Bus bus = new Bus();
        bus.setId(vehicleId);
        bus.setStatus(BUS_RUNNING);
        busService.updateById(bus);
    }

    /** 计算预计到达时间（简单模拟：按发车时间+1小时为预计到达） */
    private LocalDateTime calcEstArrive(LocalDateTime departTime) {
        return departTime.plusHours(1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TripRecord manualDispatch(Long scheduleId, Long vehicleId, Long operatorId) {
        BusSchedule schedule = scheduleService.getById(scheduleId);
        if (schedule == null) {
            throw new RuntimeException("排班记录不存在！");
        }
        if (schedule.getScheduleStatus() != null && schedule.getScheduleStatus() != SCHED_PENDING) {
            throw new RuntimeException("该排班已派车或已停运，不可重复派车！");
        }
        Bus bus = busService.getById(vehicleId);
        if (bus == null) {
            throw new RuntimeException("车辆不存在！");
        }
        if (!isDispatchable(bus)) {
            throw new RuntimeException("所选车辆当前不可派车（需在线待命或运营中）！");
        }
        // 车辆必须归属该排班线路
        if (bus.getRouteId() == null || !bus.getRouteId().equals(schedule.getRouteId())) {
            throw new RuntimeException("所选车辆不归属该线路，不可派车！");
        }

        LocalDateTime depart = toDateTime(schedule.getScheduleDate(), schedule.getDepartureTime());
        TripRecord trip = new TripRecord();
        trip.setScheduleId(scheduleId);
        trip.setRouteId(schedule.getRouteId());
        trip.setVehicleId(vehicleId);
        trip.setDirection(schedule.getDirection());
        trip.setDepartTime(depart);
        trip.setActualDepartTime(depart);
        trip.setEstArriveTime(calcEstArrive(depart));
        trip.setTripStatus(TRIP_RUNNING);
        trip.setCreateUserId(operatorId);
        this.save(trip);

        // 排班绑定车辆，状态改运行中
        schedule.setVehicleId(vehicleId);
        schedule.setScheduleStatus(SCHED_RUNNING);
        scheduleService.updateById(schedule);

        // 车辆改运营中
        setBusRunning(vehicleId);
        return trip;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TripRecord autoDispatch(Long scheduleId) {
        BusSchedule schedule = scheduleService.getById(scheduleId);
        if (schedule == null) return null;
        if (schedule.getScheduleStatus() != null && schedule.getScheduleStatus() != SCHED_PENDING) {
            return null;
        }
        Bus readyBus = findReadyBus(schedule.getRouteId());
        if (readyBus == null) {
            // 无可用车辆：排班标记为待派车
            BusSchedule update = new BusSchedule();
            update.setId(scheduleId);
            update.setScheduleStatus(SCHED_WAIT_DISPATCH);
            scheduleService.updateById(update);
            return null;
        }
        return manualDispatch(scheduleId, readyBus.getId(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TripRecord tempDispatch(Long routeId, Long vehicleId, Integer direction,
                                   LocalDateTime departTime, Long operatorId, String remark) {
        if (routeId == null || vehicleId == null) {
            throw new RuntimeException("请选择线路和车辆！");
        }
        Bus bus = busService.getById(vehicleId);
        if (bus == null || !isDispatchable(bus)) {
            throw new RuntimeException("所选车辆当前不可派车！");
        }
        // 车辆必须归属该临时加派线路
        if (bus.getRouteId() == null || !bus.getRouteId().equals(routeId)) {
            throw new RuntimeException("所选车辆不归属该线路，不可派车！");
        }
        LocalDateTime depart = departTime != null ? departTime : LocalDateTime.now();
        TripRecord trip = new TripRecord();
        trip.setScheduleId(null);
        trip.setRouteId(routeId);
        trip.setVehicleId(vehicleId);
        trip.setDirection(direction != null ? direction : 1);
        trip.setDepartTime(depart);
        trip.setActualDepartTime(depart);
        trip.setEstArriveTime(calcEstArrive(depart));
        trip.setTripStatus(TRIP_RUNNING);
        trip.setIsTemp(1);
        trip.setCreateUserId(operatorId);
        trip.setRemark(remark);
        this.save(trip);
        setBusRunning(vehicleId);
        return trip;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineVehicle(Long vehicleId, Long operatorId, String reason) {
        // 找到该车辆运行中的车次
        LambdaQueryWrapper<TripRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TripRecord::getVehicleId, vehicleId)
                .eq(TripRecord::getTripStatus, TRIP_RUNNING);
        TripRecord trip = this.getOne(wrapper, false);
        if (trip != null) {
            trip.setTripStatus(TRIP_INTERRUPT);
            trip.setInterruptReason(reason != null && !reason.isEmpty() ? reason : "调度员强制下线");
            this.updateById(trip);
            // 若来源排班在运行中，将排班改为已取消
            if (trip.getScheduleId() != null) {
                BusSchedule schedule = scheduleService.getById(trip.getScheduleId());
                if (schedule != null && schedule.getScheduleStatus() != null
                        && schedule.getScheduleStatus() == SCHED_RUNNING) {
                    BusSchedule update = new BusSchedule();
                    update.setId(schedule.getId());
                    update.setScheduleStatus(5); // 已取消
                    scheduleService.updateById(update);
                }
            }
        }
        // 车辆改待命
        setBusReady(vehicleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspendRoute(Long routeId, Long operatorId) {
        // 将该线路所有待发车排班置为已停运(4)
        LambdaUpdateWrapper<BusSchedule> update = new LambdaUpdateWrapper<>();
        update.eq(BusSchedule::getRouteId, routeId)
                .in(BusSchedule::getScheduleStatus, SCHED_PENDING, SCHED_WAIT_DISPATCH)
                .set(BusSchedule::getScheduleStatus, 4);
        scheduleService.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceVehicle(Long faultVehicleId, Long backupVehicleId, Long operatorId, String reason) {
        // 1. 故障车辆中断当前车次
        offlineVehicle(faultVehicleId, operatorId,
                reason != null && !reason.isEmpty() ? reason : "车辆故障，由备用车辆接替");

        // 2. 找出故障车辆当前待执行的排班（待发车/待派车/运行中的后续班次）
        //    简单处理：找该车辆尚未完成的排班，把车辆替换为备用车
        LambdaQueryWrapper<BusSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusSchedule::getVehicleId, faultVehicleId)
                .in(BusSchedule::getScheduleStatus, SCHED_PENDING, SCHED_WAIT_DISPATCH);
        List<BusSchedule> pendingSchedules = scheduleService.list(wrapper);

        Bus backup = busService.getById(backupVehicleId);
        if (backup == null || !isDispatchable(backup)) {
            throw new RuntimeException("备用车辆当前不可用！");
        }
        // 备用车辆必须归属故障车辆所属线路
        Bus faultBus = busService.getById(faultVehicleId);
        if (faultBus != null && faultBus.getRouteId() != null
                && (backup.getRouteId() == null || !backup.getRouteId().equals(faultBus.getRouteId()))) {
            throw new RuntimeException("备用车辆不归属该线路，不可接替！");
        }

        for (BusSchedule s : pendingSchedules) {
            // 替换车辆，保持排班待发车状态，等待到达发车时间自动派车
            BusSchedule update = new BusSchedule();
            update.setId(s.getId());
            update.setVehicleId(backupVehicleId);
            update.setScheduleStatus(SCHED_PENDING);
            scheduleService.updateById(update);
        }
        // 若没有待执行排班，则备用车辆保持待命
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTrip(Long tripId) {
        TripRecord trip = this.getById(tripId);
        if (trip == null) return;
        if (trip.getTripStatus() != null && trip.getTripStatus() == TRIP_DONE) return;

        trip.setTripStatus(TRIP_DONE);
        trip.setActualArriveTime(LocalDateTime.now());
        this.updateById(trip);

        // 释放车辆为待命
        setBusReady(trip.getVehicleId());

        // 若来源排班在运行中，改为已完成
        if (trip.getScheduleId() != null) {
            BusSchedule schedule = scheduleService.getById(trip.getScheduleId());
            if (schedule != null && schedule.getScheduleStatus() != null
                    && schedule.getScheduleStatus() == SCHED_RUNNING) {
                BusSchedule update = new BusSchedule();
                update.setId(schedule.getId());
                update.setScheduleStatus(SCHED_DONE);
                scheduleService.updateById(update);
            }
        }
    }

    /** 排班日期+时间 -> LocalDateTime */
    private LocalDateTime toDateTime(LocalDate date, LocalTime time) {
        LocalDate d = date != null ? date : LocalDate.now();
        LocalTime t = time != null ? time : LocalTime.now();
        return LocalDateTime.of(d, t);
    }

    @Override
    @Scheduled(fixedDelay = 5000)
    public void scanAndAutoDispatch() {
        // 扫描今天到达发车时间的待发车排班并自动派车
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        LambdaQueryWrapper<BusSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusSchedule::getScheduleStatus, SCHED_PENDING)
                .le(BusSchedule::getDepartureTime, currentTime)
                .and(w -> w.isNull(BusSchedule::getScheduleDate).or().eq(BusSchedule::getScheduleDate, today));
        List<BusSchedule> list = scheduleService.list(wrapper);
        for (BusSchedule s : list) {
            try {
                autoDispatch(s.getId());
            } catch (Exception e) {
                System.err.println("[自动派车] 排班 " + s.getId() + " 自动派车失败: " + e.getMessage());
            }
        }
    }

    @Override
    @Scheduled(fixedDelay = 5000)
    public void scanAndAutoComplete() {
        // 扫描运行中的车次，到达预计到达时间自动完成
        LambdaQueryWrapper<TripRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TripRecord::getTripStatus, TRIP_RUNNING)
                .le(TripRecord::getEstArriveTime, LocalDateTime.now());
        List<TripRecord> list = this.list(wrapper);
        for (TripRecord t : list) {
            try {
                completeTrip(t.getId());
            } catch (Exception e) {
                System.err.println("[自动完成] 车次 " + t.getId() + " 自动完成失败: " + e.getMessage());
            }
        }
    }
}
