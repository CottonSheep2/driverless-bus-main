package com.autonomous.bus.controller;

import com.autonomous.bus.annotation.OperLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autonomous.bus.dto.RouteDTO;
import com.autonomous.bus.entity.Route;
import com.autonomous.bus.entity.Station;
import com.autonomous.bus.service.RouteService;
import com.autonomous.bus.util.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/route")
public class RouteController {

    @Resource
    private RouteService routeService;

    @GetMapping("/page")
    public Result<Page<Route>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                    @RequestParam(defaultValue = "10") Long pageSize,
                                    @RequestParam(required = false) String routeName) {
        Page<Route> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        if (routeName != null && !routeName.isEmpty()) {
            wrapper.like(Route::getRouteName, routeName);
        }
        wrapper.orderByDesc(Route::getId);
        return Result.success(routeService.page(page, wrapper));
    }

    @GetMapping("/list")
    public Result<List<Route>> list() {
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Route::getStatus, 1).orderByAsc(Route::getRouteName);
        return Result.success(routeService.list(wrapper));
    }

    @PostMapping
    @OperLog(module = "线路管理", operation = "新增线路")
    public Result<Void> save(@RequestBody RouteDTO dto, HttpServletRequest request) {
        checkAdmin(request);
        routeService.saveRouteWithStations(dto.getRoute(), dto.getStationIds());
        return Result.success();
    }

    @PutMapping
    @OperLog(module = "线路管理", operation = "修改线路")
    public Result<Void> update(@RequestBody RouteDTO dto, HttpServletRequest request) {
        checkAdmin(request);
        routeService.saveRouteWithStations(dto.getRoute(), dto.getStationIds());
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Route> getById(@PathVariable Long id) {
        return Result.success(routeService.getById(id));
    }

    @GetMapping("/{id}/stations")
    public Result<List<Station>> getRouteStations(@PathVariable Long id) {
        return Result.success(routeService.getRouteStations(id));
    }

    @DeleteMapping("/{id}")
    @OperLog(module = "线路管理", operation = "删除线路")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        routeService.deleteRouteWithCheck(id);
        return Result.success();
    }

    private void checkAdmin(HttpServletRequest request) {
        String roleCode = (String) request.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode)) {
            throw new RuntimeException("仅系统管理员可执行此操作！");
        }
    }
}
