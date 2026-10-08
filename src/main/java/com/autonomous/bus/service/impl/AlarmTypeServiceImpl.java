package com.autonomous.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autonomous.bus.entity.AlarmReport;
import com.autonomous.bus.entity.AlarmType;
import com.autonomous.bus.mapper.AlarmReportMapper;
import com.autonomous.bus.mapper.AlarmTypeMapper;
import com.autonomous.bus.service.AlarmTypeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AlarmTypeServiceImpl extends ServiceImpl<AlarmTypeMapper, AlarmType> implements AlarmTypeService {

    @Resource
    private AlarmReportMapper alarmReportMapper;

    @Override
    public void checkBeforeDelete(Long id) {
        LambdaQueryWrapper<AlarmReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmReport::getAlarmTypeId, id);
        Long count = alarmReportMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new RuntimeException("该告警类型下还有告警记录，禁止删除！");
        }
    }
}
