package com.autonomous.bus.dto;

import lombok.Data;
import java.time.LocalDateTime;

/** 派车调度 / 临时调度 请求参数 */
@Data
public class DispatchDTO {
    /** 排班ID */
    private Long scheduleId;
    /** 车辆ID */
    private Long vehicleId;
    /** 线路ID */
    private Long routeId;
    /** 方向：1去程 2返程 */
    private Integer direction;
    /** 车辆接替：备用车辆ID */
    private Long backupVehicleId;
    /** 临时加派：发车时间 */
    private LocalDateTime departTime;
    /** 中断/下线/接替原因 */
    private String reason;
    /** 备注 */
    private String remark;
}
