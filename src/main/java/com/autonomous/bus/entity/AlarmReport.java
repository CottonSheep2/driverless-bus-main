package com.autonomous.bus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("alarm_report")
public class AlarmReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long alarmTypeId;
    private String title;
    private String content;
    private Long reporterId;
    private LocalDateTime reportTime;
    private Integer status;
    @TableLogic
    private Integer deleted;
}
