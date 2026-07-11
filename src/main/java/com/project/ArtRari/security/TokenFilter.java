package com.project.ArtRari.security;

import com.project.ArtRari.user.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @Nullable HttpServletRequest request,
            @Nullable HttpServletResponse response,
            @Nullable FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = null;
        String username = null;
        UserDetails userDetails = null;
        UsernamePasswordAuthenticationToken auth = null;
        try {
            String headerAuth = request.getHeader("Authorization");
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                jwt = headerAuth.substring(7); // 1. вытаскиваем токен из реквеста
            }
            if (jwt != null) {
                if (tokenBlacklistService.isBlacklisted(jwt)) {  // 2. проверяем нет ли токена в черном списке редис (логаут)
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                username = jwtService.getNameFromJwt(jwt); // 3. вытаскиваем из токена логин
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) { //смогли ли достать логин и пуст ли текущий контекст?
                    if (tokenBlacklistService.isBanned(username)) { // забанен ли недавно юзер с этим юзернеймом?
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"User is banned\"}");
                        return;
                    }
                    //достаем инфо о юзере из токена
                    Long userId = jwtService.getIdFromJwt(jwt);
                    String roleStr = jwtService.getRoleFromJwt(jwt);
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleStr));
                    String cleanRoleName = roleStr.replace("ROLE_", "");
                    Role userRole = Role.valueOf(cleanRoleName);
                    userDetails = new UserDetailsImpl(userId, null, null, null, username, null, userRole, false);
                    auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth); //кладем инфо об авторизации в контекст (действительно лишь на время выполнения запроса), чтоб не передавать инфо о юзере везде
                }
            }
        } catch (Exception e) {
            log.error("Неможливо встановити аутентифікацію користувача: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
/* приняли запрос - создали поток для его обработки - создали карман (секьюрити контекст для этого потока) - сработал фильтр - фильтр заглянул в карман - фильтр положил юзера в карман - обработали запрос и очистили карман*/