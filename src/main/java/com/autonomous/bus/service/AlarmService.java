package com.autonomous.bus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.autonomous.bus.entity.AlarmReport;
import com.autonomous.bus.entity.AlarmType;

import java.util.List;

public interface AlarmService extends IService<AlarmReport> {
    void reportAlarm(AlarmReport report, Long reporterId);
    void handleAlarm(Long reportId, Long handlerId, String handleContent);
    void deleteAlarmWithCheck(Long id);
    List<AlarmType> listAlarmTypes();
}
