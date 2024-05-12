package com.cafe.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerUserDetailService service;

    @Qualifier("httpServletRequest")
    @Autowired
    private ServletRequest httpServletRequest;

    private String token = null;
    private String userName = null;
    private Claims claims = null;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        if (((HttpServletRequest) httpServletRequest).getServletPath().matches("/user/login|/forgotPassword|/user/signup")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        } else {
            String authorizationHeader = ((HttpServletRequest) httpServletRequest).getHeader("Authorization");


            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
                userName = jwtUtil.extractUserName(token);
                claims = jwtUtil.extractClaims(token);
            }

            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = service.loadUserByUsername(userName);
                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthentication.setDetails(new WebAuthenticationDetailsSource().buildDetails((HttpServletRequest) httpServletRequest));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthentication);
                }
            }

            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    public boolean isAdmin(Claims claims) {
        return "admin".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isUser(Claims claims) {
        return "user".equalsIgnoreCase((String) claims.get("role"));
    }

    public String getCurrentUser() {
        return userName;
    }
}
