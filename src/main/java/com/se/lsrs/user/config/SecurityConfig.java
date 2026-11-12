package com.se.lsrs.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.se.lsrs.user.repository.UserRepository;
import com.se.lsrs.user.security.JwtAuthenticationFilter;
import com.se.lsrs.user.security.JwtProvider;

import jakarta.servlet.http.HttpServletResponse;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
	    HttpSecurity http,
            JwtProvider jwtProvider,
            UserRepository userRepository) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
	)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, e) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"code\":\"UNAUTHENTICATED\",\"message\":\"로그인이 필요합니다.\"}"
                    );
                })
                .accessDeniedHandler((request, response, e) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"code\":\"ACCESS_DENIED\",\"message\":\"접근 권한이 없습니다.\"}"
                    );
                })
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtProvider, userRepository),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
