package com.autonomous.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autonomous.bus.entity.SysRole;
import com.autonomous.bus.entity.SysUser;
import com.autonomous.bus.mapper.SysRoleMapper;
import com.autonomous.bus.mapper.SysUserMapper;
import com.autonomous.bus.service.UserService;
import com.autonomous.bus.service.TokenService;
import com.autonomous.bus.util.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private TokenService tokenService;

    @Override
    public Map<String, Object> login(String username, String password) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = this.getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用！");
        }
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误！");
        }
        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        String roleCode = role != null ? role.getRoleCode() : "SAFETY";
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCode);
        tokenService.saveToken(user.getId(), token);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("roleCode", roleCode);
        result.put("roleName", role != null ? role.getRoleName() : "");
        return result;
    }
}
