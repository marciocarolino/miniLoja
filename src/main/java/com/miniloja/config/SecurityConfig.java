package com.miniloja.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Para facilitar testes no Swagger:
        // - libera swagger e o endpoint de cadastro
        // - desabilita CSRF (senão POST via swagger/cliente falha sem token CSRF)
        // Desabilita qualquer forma de login (inclusive tela de login do Spring Security),
        // mantendo apenas as permissões dos endpoints.
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**",
                                                "/api/auth/register",
                                                "/actuator/health",
                                                "/actuator/info")
                                        .permitAll()
                                        .anyRequest()
                                        .permitAll())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .build();
    }
}
