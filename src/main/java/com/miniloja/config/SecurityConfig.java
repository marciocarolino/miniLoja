package com.miniloja.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF habilitado (mitiga CWE-352 / finding do Snyk Code).
        // Para clientes web (browser), o token será exposto em cookie (XSRF-TOKEN) e deve ser
        // enviado no header X-XSRF-TOKEN em requests mutáveis (POST/PUT/PATCH/DELETE).
        // Observação: Swagger UI e outros clientes precisarão enviar o header, caso contrário receberão 403.
        http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));

        // Endpoints liberados via permitAll (fase inicial do projeto).
        http.authorizeHttpRequests(
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
                                .permitAll());

        // Desabilita qualquer forma de login/autenticação embutida do Spring Security
        // (mantemos apenas as permissões dos endpoints acima).
        http.httpBasic(httpBasic -> httpBasic.disable());
        http.formLogin(form -> form.disable());
        http.logout(logout -> logout.disable());

        return http.build();
    }
}
