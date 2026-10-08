package com.autonomous.bus.service;

public interface TokenService {
    void saveToken(Long userId, String token);
    String getToken(Long userId);
    void deleteToken(Long userId);
    boolean validateToken(Long userId, String token);
}
