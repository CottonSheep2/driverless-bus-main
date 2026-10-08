package com.autonomous.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autonomous.bus.entity.Bus;
import com.autonomous.bus.entity.Route;
import com.autonomous.bus.entity.RouteStation;
import com.autonomous.bus.entity.Station;
import com.autonomous.bus.mapper.BusMapper;
import com.autonomous.bus.mapper.RouteMapper;
import com.autonomous.bus.mapper.RouteStationMapper;
import com.autonomous.bus.mapper.StationMapper;
import com.autonomous.bus.service.RouteService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteServiceImpl extends ServiceImpl<RouteMapper, Route> implements RouteService {

    @Resource
    private RouteStationMapper routeStationMapper;
    @Resource
    private StationMapper stationMapper;
    @Resource
    private BusMapper busMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRouteWithStations(Route route, List<Long> stationIds) {
        if (route.getId() == null) {
            this.save(route);
        } else {
            this.updateById(route);
            LambdaQueryWrapper<RouteStation> delWrapper = new LambdaQueryWrapper<>();
            delWrapper.eq(RouteStation::getRouteId, route.getId());
            routeStationMapper.delete(delWrapper);
        }
        if (stationIds != null && !stationIds.isEmpty()) {
            int seq = 1;
            for (Long stationId : stationIds) {
                RouteStation rs = new RouteStation();
                rs.setRouteId(route.getId());
                rs.setStationId(stationId);
                rs.setSeq(seq++);
                routeStationMapper.insert(rs);
            }
        }
    }

    @Override
    public void deleteRouteWithCheck(Long routeId) {
        LambdaQueryWrapper<Bus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bus::getRouteId, routeId);
        Long count = busMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new RuntimeException("该路线下还有公交车，禁止删除，请先解绑车辆！");
        }
        LambdaQueryWrapper<RouteStation> rsWrapper = new LambdaQueryWrapper<>();
        rsWrapper.eq(RouteStation::getRouteId, routeId);
        routeStationMapper.delete(rsWrapper);
        this.removeById(routeId);
    }

    @Override
    public List<Station> getRouteStations(Long routeId) {
        LambdaQueryWrapper<RouteStation> rsWrapper = new LambdaQueryWrapper<>();
        rsWrapper.eq(RouteStation::getRouteId, routeId).orderByAsc(RouteStation::getSeq);
        List<RouteStation> rsList = routeStationMapper.selectList(rsWrapper);
        List<Station> stations = new ArrayList<>();
        for (RouteStation rs : rsList) {
            Station station = stationMapper.selectById(rs.getStationId());
            if (station != null) {
                stations.add(station);
            }
        }
        return stations;
    }
}
