package com.autonomous.bus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.autonomous.bus.entity.SysUser;

import java.util.Map;

public interface UserService extends IService<SysUser> {
    Map<String, Object> login(String username, String password);
}
