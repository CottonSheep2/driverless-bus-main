package com.autonomous.bus.controller;

import com.autonomous.bus.annotation.OperLog;
import com.autonomous.bus.entity.Route;
import com.autonomous.bus.entity.Station;
import com.autonomous.bus.entity.TripRecord;
import com.autonomous.bus.service.RouteService;
import com.autonomous.bus.service.TripService;
import com.autonomous.bus.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 车次任务管理 */
@RestController
@RequestMapping("/api/trip")
public class TripController {

    @Resource
    private TripService tripService;
    @Resource
    private RouteService routeService;

    @GetMapping("/page")
    public Result<Page<TripRecord>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                         @RequestParam(defaultValue = "10") Long pageSize,
                                         @RequestParam(required = false) Long routeId,
                                         @RequestParam(required = false) Long vehicleId,
                                         @RequestParam(required = false) Integer tripStatus) {
        Page<TripRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TripRecord> wrapper = new LambdaQueryWrapper<>();
        if (routeId != null) wrapper.eq(TripRecord::getRouteId, routeId);
        if (vehicleId != null) wrapper.eq(TripRecord::getVehicleId, vehicleId);
        if (tripStatus != null) wrapper.eq(TripRecord::getTripStatus, tripStatus);
        wrapper.orderByDesc(TripRecord::getId);
        return Result.success(tripService.page(page, wrapper));
    }

    /** 车次详情：含途经站点 */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        TripRecord trip = tripService.getById(id);
        if (trip == null) {
            throw new RuntimeException("车次不存在！");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("trip", trip);
        // 途经站点
        Route route = routeService.getById(trip.getRouteId());
        String routeName = route != null ? route.getRouteName() : "";
        data.put("routeName", routeName);
        data.put("stations", getTripStations(trip.getRouteId()));
        return Result.success(data);
    }

    /** 获取线路途经站点列表 */
    private List<Map<String, Object>> getTripStations(Long routeId) {
        List<Station> stations = routeService.getRouteStations(routeId);
        List<Map<String, Object>> result = new ArrayList<>();
        int seq = 1;
        for (Station station : stations) {
            Map<String, Object> m = new HashMap<>();
            m.put("stationId", station.getId());
            m.put("stationName", station.getStationName());
            m.put("seq", seq++);
            result.add(m);
        }
        return result;
    }

    /** 手动将车次标记为已完成 */
    @PostMapping("/complete/{id}")
    @OperLog(module = "车次任务管理", operation = "车次完成")
    public Result<Void> complete(@PathVariable Long id, HttpServletRequest request) {
        tripService.completeTrip(id);
        return Result.success();
    }

    /** 运行中车次（供车辆下线选择） */
    @GetMapping("/running")
    public Result<List<TripRecord>> running() {
        LambdaQueryWrapper<TripRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TripRecord::getTripStatus, 1).orderByDesc(TripRecord::getId);
        return Result.success(tripService.list(wrapper));
    }
}
