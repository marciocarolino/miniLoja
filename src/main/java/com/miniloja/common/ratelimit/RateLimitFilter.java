package com.miniloja.common.ratelimit;

import com.miniloja.common.ratelimit.RateLimitService.RateLimitResult;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    // política confirmada
    private static final int LOGIN_MAX_PER_IP = 10;
    private static final int AUTH_MAX_PER_USER = 120;

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1) login: por IP (10/min)
        if ("/api/auth/login".equals(path) && HttpMethod.POST.matches(method)) {
            String ip = resolveClientIp(request);
            RateLimitResult result = rateLimitService.tryConsume("ip:" + ip, LOGIN_MAX_PER_IP, WINDOW);
            if (!result.allowed()) {
                writeTooManyRequests(response, result.retryAfterSeconds());
                return;
            }
        } else {
            // 2) autenticados: por usuário (120/min)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                // Nosso JwtAuthFilter usa subjectEmail como principal.
                String principal = auth.getName();
                if (principal != null && !principal.isBlank()) {
                    RateLimitResult result =
                            rateLimitService.tryConsume("user:" + principal, AUTH_MAX_PER_USER, WINDOW);
                    if (!result.allowed()) {
                        writeTooManyRequests(response, result.retryAfterSeconds());
                        return;
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private static void writeTooManyRequests(HttpServletResponse response, Long retryAfterSeconds)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        if (retryAfterSeconds != null) {
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        }
        response.getWriter().write("{\"status\":429,\"message\":\"Too many requests\"}");
    }

    /**
     * Resolução simples de IP.
     *
     * <p>Se houver proxy, a app pode ser configurada para usar ForwardedHeaderFilter
     * ou estratégia específica. Por segurança, não confiamos em X-Forwarded-For sem
     * configuração explícita do ambiente.
     */
    private static String resolveClientIp(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }
}
