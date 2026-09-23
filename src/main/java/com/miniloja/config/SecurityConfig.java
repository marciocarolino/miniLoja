package com.miniloja.config;

import com.miniloja.auth.security.JwtAuthFilter;
import com.miniloja.common.ratelimit.RateLimitFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // JWT: APIs stateless -> CSRF desabilitado (não há sessão/cookie para autenticação).
        http.csrf(csrf -> csrf.disable());

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(
                auth ->
                        auth.requestMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/api/auth/register",
                                        "/api/auth/login",
                                        "/actuator/health",
                                        "/actuator/info")
                                .permitAll()
                                .anyRequest()
                                .authenticated());

        http.httpBasic(httpBasic -> httpBasic.disable());
        http.formLogin(form -> form.disable());
        http.logout(logout -> logout.disable());

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Rate limit deve rodar depois do JWT para conseguir limitar por usuário autenticado.
        http.addFilterAfter(rateLimitFilter, JwtAuthFilter.class);

        return http.build();
    }
}
