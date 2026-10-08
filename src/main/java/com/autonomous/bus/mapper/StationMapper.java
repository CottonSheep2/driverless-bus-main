package com.autonomous.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.autonomous.bus.entity.Station;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StationMapper extends BaseMapper<Station> {
}
