package com.project.ArtRari.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogoutHandler implements LogoutHandler {
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authHeader.substring(7);
        try {
            long remainingTime = jwtService.getRemainingTimeFromJwt(token);
            tokenBlacklistService.blacklistToken(token, remainingTime);
            log.info("Користувач {} вийшов з акаунту", jwtService.getIdFromJwt(token));
        } catch (Exception e) {
            log.error("Помилка під час логауту: {}", e.getMessage(), e);
        }
    }
}
