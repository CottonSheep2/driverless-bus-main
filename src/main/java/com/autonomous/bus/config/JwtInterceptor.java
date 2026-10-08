package com.autonomous.bus.config;

import com.autonomous.bus.service.TokenService;
import com.autonomous.bus.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    public JwtInterceptor(JwtUtil jwtUtil, TokenService tokenService) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);
                Long userId = Long.valueOf(claims.get("userId").toString());
                if (!tokenService.validateToken(userId, token)) {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"msg\":\"登录已失效，请重新登录\"}");
                    return false;
                }
                String username = claims.get("username") != null ? claims.get("username").toString() : "unknown";
                String roleCode = claims.get("roleCode").toString();
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("roleCode", roleCode);
                return true;
            } catch (Exception e) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"token无效或已过期\"}");
                return false;
            }
        }
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录，请先登录\"}");
        } catch (Exception e) {
            // ignore
        }
        return false;
    }
}
