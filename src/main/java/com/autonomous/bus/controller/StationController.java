package com.autonomous.bus.controller;

import com.autonomous.bus.annotation.OperLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autonomous.bus.entity.Station;
import com.autonomous.bus.service.StationService;
import com.autonomous.bus.util.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/station")
public class StationController {

    @Resource
    private StationService stationService;

    @GetMapping("/page")
    public Result<Page<Station>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                      @RequestParam(defaultValue = "10") Long pageSize,
                                      @RequestParam(required = false) String stationName,
                                      @RequestParam(required = false) Integer status) {
        Page<Station> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        if (stationName != null && !stationName.isEmpty()) {
            wrapper.like(Station::getStationName, stationName);
        }
        if (status != null) {
            wrapper.eq(Station::getStatus, status);
        }
        wrapper.orderByDesc(Station::getId);
        return Result.success(stationService.page(page, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Station>> list() {
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Station::getStatus, 1).orderByAsc(Station::getStationName);
        return Result.success(stationService.list(wrapper));
    }

    @PostMapping
    @OperLog(module = "站点管理", operation = "新增站点")
    public Result<Void> save(@RequestBody Station station, HttpServletRequest request) {
        checkAdmin(request);
        stationService.save(station);
        return Result.success();
    }

    @PutMapping
    @OperLog(module = "站点管理", operation = "修改站点")
    public Result<Void> update(@RequestBody Station station, HttpServletRequest request) {
        checkAdmin(request);
        stationService.updateById(station);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Station> getById(@PathVariable Long id) {
        return Result.success(stationService.getById(id));
    }

    @DeleteMapping("/{id}")
    @OperLog(module = "站点管理", operation = "删除站点")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        stationService.checkBeforeDelete(id);
        stationService.removeById(id);
        return Result.success();
    }

    private void checkAdmin(HttpServletRequest request) {
        String roleCode = (String) request.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode)) {
            throw new RuntimeException("仅系统管理员可执行此操作！");
        }
    }
}
