package com.autonomous.bus.controller;

import com.autonomous.bus.annotation.OperLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autonomous.bus.dto.AlarmHandleDTO;
import com.autonomous.bus.entity.AlarmReport;
import com.autonomous.bus.entity.AlarmType;
import com.autonomous.bus.service.AlarmService;
import com.autonomous.bus.util.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarm")
public class AlarmController {

    @Resource
    private AlarmService alarmService;

    @GetMapping("/page")
    public Result<Page<AlarmReport>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                          @RequestParam(defaultValue = "10") Long pageSize,
                                          @RequestParam(required = false) String title,
                                          @RequestParam(required = false) Integer status) {
        Page<AlarmReport> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AlarmReport> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(AlarmReport::getTitle, title);
        }
        if (status != null) {
            wrapper.eq(AlarmReport::getStatus, status);
        }
        wrapper.orderByDesc(AlarmReport::getId);
        return Result.success(alarmService.page(page, wrapper));
    }

    @GetMapping("/types")
    public Result<List<AlarmType>> types() {
        return Result.success(alarmService.listAlarmTypes());
    }

    @PostMapping("/report")
    @OperLog(module = "告警管理", operation = "上报告警")
    public Result<Void> report(@RequestBody AlarmReport report, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        alarmService.reportAlarm(report, userId);
        return Result.success();
    }

    @PostMapping("/handle")
    @OperLog(module = "告警管理", operation = "处理告警")
    public Result<Void> handle(@RequestBody AlarmHandleDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        alarmService.handleAlarm(dto.getReportId(), userId, dto.getHandleContent());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperLog(module = "告警管理", operation = "删除告警")
    public Result<Void> delete(@PathVariable Long id) {
        alarmService.deleteAlarmWithCheck(id);
        return Result.success();
    }
}
