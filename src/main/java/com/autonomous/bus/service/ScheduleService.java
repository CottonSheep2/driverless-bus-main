package com.autonomous.bus.service;

import com.autonomous.bus.entity.BusSchedule;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleService extends IService<BusSchedule> {

    /**
     * 手动新增单条排班
     */
    void addSingleSchedule(BusSchedule schedule, Long userId);

    /**
     * 批量生成排班（首班-末班-间隔）
     * @return 生成的排班数量
     */
    int batchGenerate(Long routeId, Integer direction, java.time.LocalDate scheduleDate,
                      LocalTime firstTime, LocalTime lastTime, Integer intervalMin, Long userId);

    /**
     * 删除排班（未发车的排班才允许删除）
     */
    void deleteSchedule(Long id);

    /**
     * 查询某线路下待发车/待派车的排班（供手动派车选择）
     */
    List<BusSchedule> listPendingByRoute(Long routeId);
}
