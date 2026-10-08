package com.autonomous.bus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autonomous.bus.entity.Bus;
import com.autonomous.bus.mapper.BusMapper;
import com.autonomous.bus.service.BusService;
import org.springframework.stereotype.Service;

@Service
public class BusServiceImpl extends ServiceImpl<BusMapper, Bus> implements BusService {
}
