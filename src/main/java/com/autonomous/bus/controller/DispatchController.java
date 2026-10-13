package com.autonomous.bus.controller;

import com.autonomous.bus.annotation.OperLog;
import com.autonomous.bus.dto.DispatchDTO;
import com.autonomous.bus.entity.Bus;
import com.autonomous.bus.entity.TripRecord;
import com.autonomous.bus.service.BusService;
import com.autonomous.bus.service.TripService;
import com.autonomous.bus.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 派车调度 + 临时调度 */
@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    @Resource
    private TripService tripService;
    @Resource
    private BusService busService;

    /** 手动派车：选择待发车排班 + 空闲车辆 */
    @PostMapping("/manual")
    @OperLog(module = "派车调度", operation = "手动派车")
    public Result<TripRecord> manual(@RequestBody DispatchDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TripRecord trip = tripService.manualDispatch(dto.getScheduleId(), dto.getVehicleId(), userId);
        return Result.success(trip);
    }

    /** 临时加派班次：不依赖排班直接派车 */
    @PostMapping("/temp")
    @OperLog(module = "临时调度", operation = "临时加派班次")
    public Result<TripRecord> temp(@RequestBody DispatchDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TripRecord trip = tripService.tempDispatch(dto.getRouteId(), dto.getVehicleId(),
                dto.getDirection(), dto.getDepartTime(), userId, dto.getRemark());
        return Result.success(trip);
    }

    /** 车辆下线：强制下线运行中的车辆 */
    @PostMapping("/offline")
    @OperLog(module = "临时调度", operation = "车辆下线")
    public Result<Void> offline(@RequestBody DispatchDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        tripService.offlineVehicle(dto.getVehicleId(), userId, dto.getReason());
        return Result.success();
    }

    /** 临时停运：整条线路暂停 */
    @PostMapping("/suspend")
    @OperLog(module = "临时调度", operation = "线路临时停运")
    public Result<Void> suspend(@RequestBody DispatchDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        tripService.suspendRoute(dto.getRouteId(), userId);
        return Result.success();
    }

    /** 车辆接替（应急调度）：故障车辆由备用车辆接替后续班次 */
    @PostMapping("/replace")
    @OperLog(module = "临时调度", operation = "车辆接替")
    public Result<Void> replace(@RequestBody DispatchDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (dto.getBackupVehicleId() == null) {
            throw new RuntimeException("请选择备用车辆！");
        }
        tripService.replaceVehicle(dto.getVehicleId(), dto.getBackupVehicleId(), userId, dto.getReason());
        return Result.success();
    }

    /** 查询可派遣车辆（在线待命1 或 运营中4、电量充足），供派车选择 */
    @GetMapping("/ready-buses")
    public Result<List<Bus>> readyBuses(@RequestParam(required = false) Long routeId) {
        LambdaQueryWrapper<Bus> wrapper = new LambdaQueryWrapper<>();
        // 在线待命(1) 与 运营中(4) 的车辆均可派遣
        wrapper.in(Bus::getStatus, 1, 4);
        wrapper.and(w -> w.isNull(Bus::getBattery).or().ge(Bus::getBattery, 20));
        if (routeId != null) {
            wrapper.eq(Bus::getRouteId, routeId);
        }
        wrapper.orderByAsc(Bus::getRouteId);
        return Result.success(busService.list(wrapper));
    }
}
