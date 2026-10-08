package com.autonomous.bus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("route_station")
public class RouteStation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long routeId;
    private Long stationId;
    private Integer seq;
    private LocalDateTime createTime;
}
