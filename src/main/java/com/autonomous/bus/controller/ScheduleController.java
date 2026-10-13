package com.autonomous.bus.controller;

import com.autonomous.bus.annotation.OperLog;
import com.autonomous.bus.dto.ScheduleBatchDTO;
import com.autonomous.bus.entity.BusSchedule;
import com.autonomous.bus.service.ScheduleService;
import com.autonomous.bus.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/** 排班计划管理（发车时刻表） */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    @GetMapping("/page")
    public Result<Page<BusSchedule>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                          @RequestParam(defaultValue = "10") Long pageSize,
                                          @RequestParam(required = false) Long routeId,
                                          @RequestParam(required = false) Integer direction,
                                          @RequestParam(required = false) Integer scheduleStatus) {
        Page<BusSchedule> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BusSchedule> wrapper = new LambdaQueryWrapper<>();
        if (routeId != null) wrapper.eq(BusSchedule::getRouteId, routeId);
        if (direction != null) wrapper.eq(BusSchedule::getDirection, direction);
        if (scheduleStatus != null) wrapper.eq(BusSchedule::getScheduleStatus, scheduleStatus);
        wrapper.orderByDesc(BusSchedule::getId);
        return Result.success(scheduleService.page(page, wrapper));
    }

    /** 手动新增单条排班 */
    @PostMapping("/single")
    @OperLog(module = "排班计划管理", operation = "新增单条排班")
    public Result<Void> addSingle(@RequestBody BusSchedule schedule, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        scheduleService.addSingleSchedule(schedule, userId);
        return Result.success();
    }

    /** 批量生成排班（首班-末班-发车间隔） */
    @PostMapping("/batch")
    @OperLog(module = "排班计划管理", operation = "批量生成排班")
    public Result<Map<String, Object>> batch(@RequestBody ScheduleBatchDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = scheduleService.batchGenerate(dto.getRouteId(), dto.getDirection(),
                dto.getScheduleDate(), dto.getFirstTime(), dto.getLastTime(),
                dto.getIntervalMin(), userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    /** 编辑排班 */
    @PutMapping
    @OperLog(module = "排班计划管理", operation = "修改排班")
    public Result<Void> update(@RequestBody BusSchedule schedule) {
        if (schedule.getId() == null) {
            throw new RuntimeException("排班ID不能为空！");
        }
        scheduleService.updateById(schedule);
        return Result.success();
    }

    /** 删除排班 */
    @DeleteMapping("/{id}")
    @OperLog(module = "排班计划管理", operation = "删除排班")
    public Result<Void> delete(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return Result.success();
    }
}
