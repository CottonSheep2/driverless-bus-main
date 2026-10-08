package com.autonomous.bus.aspect;

import com.autonomous.bus.annotation.OperLog;
import com.autonomous.bus.entity.OperationLog;
import com.autonomous.bus.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class OperLogAspect {

    @Resource
    private OperationLogService operationLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        System.out.println("[操作日志] 切面拦截到: " + operLog.module() + " - " + operLog.operation());
        long startTime = System.currentTimeMillis();
        OperationLog log = new OperationLog();
        log.setModule(operLog.module());
        log.setOperation(operLog.operation());
        log.setCreateTime(LocalDateTime.now());

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            // 从方法参数中提取用户名（登录场景request中还没有用户信息）
            String paramUsername = null;
            if (paramNames != null && args != null) {
                for (Object arg : args) {
                    if (arg instanceof com.autonomous.bus.dto.LoginDTO) {
                        paramUsername = ((com.autonomous.bus.dto.LoginDTO) arg).getUsername();
                        break;
                    }
                }
            }

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Long userId = (Long) request.getAttribute("userId");
                String username = (String) request.getAttribute("username");
                log.setUserId(userId);
                log.setUsername(username != null ? username : (paramUsername != null ? paramUsername : "unknown"));
                log.setIp(getIp(request));
                log.setMethod(request.getMethod() + " " + request.getRequestURI());
            }

            if (paramNames != null && args != null) {
                StringBuilder params = new StringBuilder();
                for (int i = 0; i < paramNames.length; i++) {
                    if (args[i] instanceof HttpServletRequest) continue;
                    params.append(paramNames[i]).append("=");
                    try {
                        params.append(objectMapper.writeValueAsString(args[i]));
                    } catch (Exception e) {
                        params.append(args[i]);
                    }
                    if (i < paramNames.length - 1) params.append(", ");
                }
                if (params.length() > 2000) {
                    log.setParams(params.substring(0, 2000) + "...");
                } else {
                    log.setParams(params.toString());
                }
            }

            Object result = joinPoint.proceed();
            log.setStatus(1);
            return result;
        } catch (Throwable e) {
            log.setStatus(0);
            log.setErrorMsg(e.getMessage() != null ? e.getMessage().substring(0, Math.min(500, e.getMessage().length())) : "error");
            throw e;
        } finally {
            log.setCostTime(System.currentTimeMillis() - startTime);
            try {
                operationLogService.save(log);
                System.out.println("[操作日志] 已记录: " + log.getModule() + " - " + log.getOperation() + " | 用户: " + log.getUsername());
            } catch (Exception e) {
                System.err.println("[操作日志] 保存失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
