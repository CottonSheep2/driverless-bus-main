package com.autonomous.bus.service.impl;

import com.autonomous.bus.entity.BusSchedule;
import com.autonomous.bus.mapper.BusScheduleMapper;
import com.autonomous.bus.service.BusService;
import com.autonomous.bus.service.ScheduleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleServiceImpl extends ServiceImpl<BusScheduleMapper, BusSchedule> implements ScheduleService {

    @Resource
    private BusService busService;

    @Override
    public void addSingleSchedule(BusSchedule schedule, Long userId) {
        if (schedule.getRouteId() == null) {
            throw new RuntimeException("请选择排班线路！");
        }
        if (schedule.getDepartureTime() == null) {
            throw new RuntimeException("请设置发车时间！");
        }
        schedule.setGenerateMode(0);
        schedule.setScheduleStatus(0); // 待发车
        schedule.setCreateUserId(userId);
        this.save(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGenerate(Long routeId, Integer direction, LocalDate scheduleDate,
                             LocalTime firstTime, LocalTime lastTime, Integer intervalMin, Long userId) {
        if (routeId == null) {
            throw new RuntimeException("请选择排班线路！");
        }
        if (firstTime == null || lastTime == null || intervalMin == null) {
            throw new RuntimeException("请填写首班、末班和发车间隔！");
        }
        if (!lastTime.isAfter(firstTime)) {
            throw new RuntimeException("末班时间必须晚于首班时间！");
        }
        if (intervalMin <= 0) {
            throw new RuntimeException("发车间隔必须大于0分钟！");
        }

        List<BusSchedule> list = new ArrayList<>();
        LocalTime cur = firstTime;
        int count = 0;
        while (!cur.isAfter(lastTime)) {
            BusSchedule s = new BusSchedule();
            s.setRouteId(routeId);
            s.setDirection(direction != null ? direction : 1);
            s.setScheduleDate(scheduleDate);
            s.setDepartureTime(cur);
            s.setGenerateMode(1);
            s.setFirstTime(firstTime);
            s.setLastTime(lastTime);
            s.setIntervalMin(intervalMin);
            s.setScheduleStatus(0); // 待发车
            s.setCreateUserId(userId);
            list.add(s);
            cur = cur.plusMinutes(intervalMin);
            count++;
            // 防止死循环
            if (count > 500) break;
        }
        this.saveBatch(list);
        return count;
    }

    @Override
    public void deleteSchedule(Long id) {
        BusSchedule schedule = this.getById(id);
        if (schedule == null) {
            throw new RuntimeException("排班记录不存在！");
        }
        if (schedule.getScheduleStatus() != null && schedule.getScheduleStatus() == 2) {
            throw new RuntimeException("排班已在运行中，不可删除！");
        }
        if (schedule.getScheduleStatus() != null && schedule.getScheduleStatus() == 1) {
            throw new RuntimeException("排班已派车，请先取消车辆后再删除！");
        }
        this.removeById(id);
    }

    @Override
    public List<BusSchedule> listPendingByRoute(Long routeId) {
        LambdaQueryWrapper<BusSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusSchedule::getRouteId, routeId)
                .in(BusSchedule::getScheduleStatus, 0, 1)
                .orderByAsc(BusSchedule::getDepartureTime);
        return this.list(wrapper);
    }
}
