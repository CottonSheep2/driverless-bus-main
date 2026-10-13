package com.autonomous.bus.service;

import com.autonomous.bus.entity.TripRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TripService extends IService<TripRecord> {

    /**
     * 手动派车：把一条待发车排班绑定指定车辆并创建车次
     * @return 生成的车次
     */
    TripRecord manualDispatch(Long scheduleId, Long vehicleId, Long operatorId);

    /**
     * 自动派车（排班到达发车时间触发）：自动匹配空闲车辆
     * @param scheduleId 排班ID
     * @return 生成的车次，无可用车辆返回null（排班标记为待派车）
     */
    TripRecord autoDispatch(Long scheduleId);

    /**
     * 临时加派班次：不依赖排班，直接创建车次并派车
     */
    TripRecord tempDispatch(Long routeId, Long vehicleId, Integer direction,
                            java.time.LocalDateTime departTime, Long operatorId, String remark);

    /**
     * 车辆下线：停止行驶任务，车辆改为待命，当前车次标记中断
     */
    void offlineVehicle(Long vehicleId, Long operatorId, String reason);

    /**
     * 临时停运：线路所有待发车排班置为已停运
     */
    void suspendRoute(Long routeId, Long operatorId);

    /**
     * 车辆接替（应急调度）：故障车辆车次中断，备用车辆接替后续班次
     */
    void replaceVehicle(Long faultVehicleId, Long backupVehicleId, Long operatorId, String reason);

    /**
     * 车次到达终点，自动标记已完成并释放车辆为待命
     */
    void completeTrip(Long tripId);

    /**
     * 定时任务：扫描到达发车时间的待发车排班并自动派车
     */
    void scanAndAutoDispatch();

    /**
     * 定时任务：扫描运行中的车次，到预计到达时间自动完成
     */
    void scanAndAutoComplete();
}
