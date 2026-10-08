package com.autonomous.bus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autonomous.bus.entity.OperationLog;
import com.autonomous.bus.mapper.OperationLogMapper;
import com.autonomous.bus.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
}
