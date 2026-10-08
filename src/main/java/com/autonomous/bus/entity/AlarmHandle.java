package com.autonomous.bus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("alarm_handle")
public class AlarmHandle {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reportId;
    private Long handlerId;
    private String handleContent;
    private LocalDateTime handleTime;
    @TableLogic
    private Integer deleted;
}
