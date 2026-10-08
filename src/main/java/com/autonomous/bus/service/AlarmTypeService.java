package com.autonomous.bus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.autonomous.bus.entity.AlarmType;

public interface AlarmTypeService extends IService<AlarmType> {
    void checkBeforeDelete(Long id);
}
