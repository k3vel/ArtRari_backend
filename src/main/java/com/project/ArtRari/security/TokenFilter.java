package com.project.ArtRari.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class TokenFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
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
                username = jwtService.getNameFromJwt(jwt); // 2. вытаскиваем из токена логин
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) { //смогли ли достать логин и пуст ли текущий контекст?
                    userDetails = userDetailsService.loadUserByUsername(username); //достаем инфо о юзере из бд. если не нашли то ошибка - этого юзера нет в бд но он как-то получил токен
                    auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth); //кладем инфо об авторизации (действительно лишь на время выполнения запроса, чтоб не передавать инфо о юзере везде)
                }
            }
        } catch (Exception e) {
            //todo
        }
        filterChain.doFilter(request, response);
    }
}
/* приняли запрос - создали поток для его обработки - создали карман (секьюрити контекст для этого потока) - сработал фильтр - фильтр заглянул в карман - фильтр положил юзера в карман - обработали запрос и очистили карман*/