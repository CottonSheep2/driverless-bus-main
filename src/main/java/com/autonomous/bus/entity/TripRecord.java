package com.autonomous.bus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("trip_record")
public class TripRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 来源排班ID(临时加派为null) */
    private Long scheduleId;
    private Long routeId;
    private Long vehicleId;
    /** 方向：1去程 2返程 */
    private Integer direction;
    /** 计划发车时间 */
    private LocalDateTime departTime;
    /** 实际发车时间 */
    private LocalDateTime actualDepartTime;
    /** 预计到达时间 */
    private LocalDateTime estArriveTime;
    /** 实际到达时间 */
    private LocalDateTime actualArriveTime;
    /** 车次状态：0待发车 1运行中 2已完成 3已中断 */
    private Integer tripStatus;
    /** 中断原因 */
    private String interruptReason;
    /** 是否临时加派：0否 1是 */
    private Integer isTemp;
    /** 创建人ID */
    private Long createUserId;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
