package com.smartstay.reports.config;

import com.smartstay.reports.ennum.ServiceEnum;
import com.smartstay.reports.service.JwtService;
import com.smartstay.reports.service.MyUserDetailsService;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String userName = null;

            if (authHeader != null && authHeader.startsWith("Bearer")) {
                token = authHeader.substring(7);
                userName = ServiceEnum.reports.name();
            }

            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails details = applicationContext.getBean(MyUserDetailsService.class)
                        .loadUserByUsername(userName);

                if (jwtService.validateServiceToken(token, details)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(details,
                                    null, details.getAuthorities());

                    authToken.setDetails(jwtService.extractAllClaims(token, details.getPassword()));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (SignatureException se) {
            throw new SignatureException("Please login again");
        }
    }
}
