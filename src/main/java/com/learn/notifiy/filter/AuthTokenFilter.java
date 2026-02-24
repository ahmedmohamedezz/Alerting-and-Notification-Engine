package com.learn.notifiy.filter;

import com.learn.notifiy.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver resolver;

    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                // this route shouldn't be authenticated
                filterChain.doFilter(request, response);
                return;
            }

            // make sure jwt token is sent & is valid
            String token = jwtUtils.extractTokenFromRequestHeaders(request);
            jwtUtils.validateAccessToken(token);

            String email = jwtUtils.getSubjectFromAccessToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // create authentication object & set security context
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // pass to next filter
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // how to propagate exception ? will spring handle it, or app crash ?
            logger.error("Auth filter error. {}", e.getMessage());

            // pass to global exception handler
            resolver.resolveException(request, response, null, e);
        }
    }
}
