package com.autonomous.bus.dto;

import com.autonomous.bus.entity.Route;
import lombok.Data;
import java.util.List;

@Data
public class RouteDTO {
    private Route route;
    private List<Long> stationIds;
}
