package com.autonomous.bus.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/** 批量生成排班请求参数 */
@Data
public class ScheduleBatchDTO {
    private Long routeId;
    /** 方向：1去程 2返程 */
    private Integer direction;
    /** 排班日期（留空为每日通用） */
    private LocalDate scheduleDate;
    /** 首班时间 */
    private LocalTime firstTime;
    /** 末班时间 */
    private LocalTime lastTime;
    /** 发车间隔(分钟) */
    private Integer intervalMin;
}
