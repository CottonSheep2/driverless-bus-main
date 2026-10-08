package com.autonomous.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autonomous.bus.entity.AlarmHandle;
import com.autonomous.bus.entity.AlarmReport;
import com.autonomous.bus.entity.AlarmType;
import com.autonomous.bus.mapper.AlarmHandleMapper;
import com.autonomous.bus.mapper.AlarmReportMapper;
import com.autonomous.bus.mapper.AlarmTypeMapper;
import com.autonomous.bus.service.AlarmService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlarmServiceImpl extends ServiceImpl<AlarmReportMapper, AlarmReport> implements AlarmService {

    @Resource
    private AlarmHandleMapper alarmHandleMapper;
    @Resource
    private AlarmTypeMapper alarmTypeMapper;

    @Override
    public void reportAlarm(AlarmReport report, Long reporterId) {
        report.setReporterId(reporterId);
        report.setReportTime(LocalDateTime.now());
        report.setStatus(0);
        this.save(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlarm(Long reportId, Long handlerId, String handleContent) {
        AlarmReport report = this.getById(reportId);
        if (report == null) {
            throw new RuntimeException("告警记录不存在！");
        }
        if (report.getStatus() != null && report.getStatus() == 1) {
            throw new RuntimeException("该告警已处理，请勿重复操作！");
        }
        AlarmHandle handle = new AlarmHandle();
        handle.setReportId(reportId);
        handle.setHandlerId(handlerId);
        handle.setHandleContent(handleContent);
        handle.setHandleTime(LocalDateTime.now());
        alarmHandleMapper.insert(handle);

        report.setStatus(1);
        this.updateById(report);
    }

    @Override
    public void deleteAlarmWithCheck(Long id) {
        AlarmReport report = this.getById(id);
        if (report == null) {
            throw new RuntimeException("告警记录不存在！");
        }
        if (report.getStatus() == null || report.getStatus() != 1) {
            throw new RuntimeException("仅已处理的告警允许删除！");
        }
        LambdaQueryWrapper<AlarmHandle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmHandle::getReportId, id);
        alarmHandleMapper.delete(wrapper);
        this.removeById(id);
    }

    @Override
    public List<AlarmType> listAlarmTypes() {
        LambdaQueryWrapper<AlarmType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmType::getStatus, 1);
        return alarmTypeMapper.selectList(wrapper);
    }
}
