package com.autonomous.bus.controller;

import com.autonomous.bus.annotation.OperLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autonomous.bus.dto.LoginDTO;
import com.autonomous.bus.entity.SysRole;
import com.autonomous.bus.entity.SysUser;
import com.autonomous.bus.mapper.SysRoleMapper;
import com.autonomous.bus.service.UserService;
import com.autonomous.bus.service.TokenService;
import com.autonomous.bus.util.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private TokenService tokenService;

    @PostMapping("/login")
    @OperLog(module = "系统管理", operation = "用户登录")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto.getUsername(), dto.getPassword()));
    }

    @PostMapping("/logout")
    @OperLog(module = "系统管理", operation = "用户登出")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) {
            tokenService.deleteToken(userId);
        }
        return Result.success();
    }

    @GetMapping("/page")
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                      @RequestParam(defaultValue = "10") Long pageSize,
                                      @RequestParam(required = false) String username) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysUser::getUsername, username);
        }
        wrapper.orderByDesc(SysUser::getId);
        return Result.success(userService.page(page, wrapper));
    }

    @GetMapping("/roles")
    public Result<List<SysRole>> roles() {
        return Result.success(sysRoleMapper.selectList(null));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysUser user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456");
        }
        userService.save(user);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(null);
        }
        userService.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
}
