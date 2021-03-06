package com.ksu.soccerserver.config;

import com.ksu.soccerserver.auth.ExpiredTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequiredArgsConstructor
//인증 작업을 진행하는 Filter
//검증이 끝난 JWT로부터 유저정보를 받아와 UsernamePasswordAuthenticationFilter로 전달
public class JwtAuthenticationFilter extends GenericFilterBean {
    private final JwtTokenProvider jwtTokenProvider;
    private final ExpiredTokenRepository expiredTokenRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //헤더에서 JWT를 받아온다.
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);

        if (token != null && !expiredTokenRepository.findByToken(token).isPresent() && jwtTokenProvider.validateToken(token)) {
            //토큰이 유효하면 토큰으로부터 유저 정보 받아온다.
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            //SecurityContext에 Authentication 객체 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        try{
            chain.doFilter(request, response);
        } catch (RuntimeException e) {
            
            //throw new BadCredentialsException("Not used Token");
        }
    }
}
