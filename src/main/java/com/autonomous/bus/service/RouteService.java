package com.autonomous.bus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.autonomous.bus.entity.Route;
import com.autonomous.bus.entity.Station;

import java.util.List;

public interface RouteService extends IService<Route> {
    void saveRouteWithStations(Route route, List<Long> stationIds);
    void deleteRouteWithCheck(Long routeId);
    List<Station> getRouteStations(Long routeId);
}
