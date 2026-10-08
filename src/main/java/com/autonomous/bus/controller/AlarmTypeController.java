package com.autonomous.bus.controller;

import com.autonomous.bus.annotation.OperLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autonomous.bus.entity.AlarmType;
import com.autonomous.bus.service.AlarmTypeService;
import com.autonomous.bus.util.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarm-type")
public class AlarmTypeController {

    @Resource
    private AlarmTypeService alarmTypeService;

    @GetMapping("/page")
    public Result<Page<AlarmType>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                        @RequestParam(defaultValue = "10") Long pageSize,
                                        @RequestParam(required = false) String typeName) {
        Page<AlarmType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AlarmType> wrapper = new LambdaQueryWrapper<>();
        if (typeName != null && !typeName.isEmpty()) {
            wrapper.like(AlarmType::getTypeName, typeName);
        }
        wrapper.orderByDesc(AlarmType::getId);
        return Result.success(alarmTypeService.page(page, wrapper));
    }

    @GetMapping("/list")
    public Result<List<AlarmType>> list() {
        LambdaQueryWrapper<AlarmType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmType::getStatus, 1).orderByAsc(AlarmType::getTypeName);
        return Result.success(alarmTypeService.list(wrapper));
    }

    @PostMapping
    @OperLog(module = "告警类型管理", operation = "新增告警类型")
    public Result<Void> save(@RequestBody AlarmType alarmType, HttpServletRequest request) {
        checkAdmin(request);
        alarmTypeService.save(alarmType);
        return Result.success();
    }

    @PutMapping
    @OperLog(module = "告警类型管理", operation = "修改告警类型")
    public Result<Void> update(@RequestBody AlarmType alarmType, HttpServletRequest request) {
        checkAdmin(request);
        alarmTypeService.updateById(alarmType);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperLog(module = "告警类型管理", operation = "删除告警类型")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        alarmTypeService.checkBeforeDelete(id);
        alarmTypeService.removeById(id);
        return Result.success();
    }

    private void checkAdmin(HttpServletRequest request) {
        String roleCode = (String) request.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode)) {
            throw new RuntimeException("仅系统管理员可操作告警类型！");
        }
    }
}
