package com.autonomous.bus.controller;

import com.autonomous.bus.annotation.OperLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autonomous.bus.entity.Bus;
import com.autonomous.bus.service.BusService;
import com.autonomous.bus.util.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bus")
public class BusController {

    @Resource
    private BusService busService;

    @GetMapping("/page")
    public Result<Page<Bus>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                  @RequestParam(defaultValue = "10") Long pageSize,
                                  @RequestParam(required = false) String plateNumber,
                                  @RequestParam(required = false) Integer status) {
        Page<Bus> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Bus> wrapper = new LambdaQueryWrapper<>();
        if (plateNumber != null && !plateNumber.isEmpty()) {
            wrapper.like(Bus::getPlateNumber, plateNumber);
        }
        if (status != null) {
            wrapper.eq(Bus::getStatus, status);
        }
        wrapper.orderByDesc(Bus::getId);
        return Result.success(busService.page(page, wrapper));
    }

    @PostMapping
    @OperLog(module = "公交车管理", operation = "新增车辆")
    public Result<Void> save(@RequestBody Bus bus, HttpServletRequest request) {
        checkAdmin(request);
        busService.save(bus);
        return Result.success();
    }

    @PutMapping
    @OperLog(module = "公交车管理", operation = "修改车辆")
    public Result<Void> update(@RequestBody Bus bus, HttpServletRequest request) {
        checkAdmin(request);
        busService.updateById(bus);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Bus> getById(@PathVariable Long id) {
        return Result.success(busService.getById(id));
    }

    @DeleteMapping("/{id}")
    @OperLog(module = "公交车管理", operation = "删除车辆")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        busService.removeById(id);
        return Result.success();
    }

    private void checkAdmin(HttpServletRequest request) {
        String roleCode = (String) request.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode)) {
            throw new RuntimeException("仅系统管理员可执行此操作！");
        }
    }
}
