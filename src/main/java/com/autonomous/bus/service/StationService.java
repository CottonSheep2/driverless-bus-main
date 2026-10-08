package com.autonomous.bus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.autonomous.bus.entity.Station;

public interface StationService extends IService<Station> {
    void checkBeforeDelete(Long stationId);
}
