package com.autonomous.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autonomous.bus.entity.RouteStation;
import com.autonomous.bus.entity.Station;
import com.autonomous.bus.mapper.RouteStationMapper;
import com.autonomous.bus.mapper.StationMapper;
import com.autonomous.bus.service.StationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements StationService {

    @Resource
    private RouteStationMapper routeStationMapper;

    @Override
    public void checkBeforeDelete(Long stationId) {
        LambdaQueryWrapper<RouteStation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RouteStation::getStationId, stationId);
        Long count = routeStationMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new RuntimeException("该站点已被路线引用，禁止删除，请先解除关联！");
        }
    }
}
