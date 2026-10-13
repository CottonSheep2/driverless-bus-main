package com.autonomous.bus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("bus_schedule")
public class BusSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long routeId;
    /** 方向：1去程 2返程 */
    private Integer direction;
    /** 排班日期(留空表示每日通用) */
    private LocalDate scheduleDate;
    /** 发车时间 */
    private LocalTime departureTime;
    /** 生成方式：0手动单条 1批量生成 */
    private Integer generateMode;
    /** 批量模式：首班时间 */
    private LocalTime firstTime;
    /** 批量模式：末班时间 */
    private LocalTime lastTime;
    /** 批量模式：发车间隔(分钟) */
    private Integer intervalMin;
    /** 排班状态：0待发车 1待派车 2运行中 3已完成 4已停运 5已取消 */
    private Integer scheduleStatus;
    /** 绑定车辆ID */
    private Long vehicleId;
    /** 创建人ID */
    private Long createUserId;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
