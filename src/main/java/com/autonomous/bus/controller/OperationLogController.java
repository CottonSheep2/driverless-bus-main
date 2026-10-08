package com.autonomous.bus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autonomous.bus.entity.OperationLog;
import com.autonomous.bus.service.OperationLogService;
import com.autonomous.bus.util.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;

    @GetMapping("/page")
    public Result<Page<OperationLog>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                           @RequestParam(defaultValue = "10") Long pageSize,
                                           @RequestParam(required = false) String module,
                                           @RequestParam(required = false) String username) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (username != null && !username.isEmpty()) {
            wrapper.like(OperationLog::getUsername, username);
        }
        wrapper.orderByDesc(OperationLog::getId);
        return Result.success(operationLogService.page(page, wrapper));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        operationLogService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/clean")
    public Result<Void> clean(HttpServletRequest request) {
        checkAdmin(request);
        operationLogService.remove(null);
        return Result.success();
    }

    private void checkAdmin(HttpServletRequest request) {
        String roleCode = (String) request.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode)) {
            throw new RuntimeException("仅系统管理员可操作日志！");
        }
    }
}
