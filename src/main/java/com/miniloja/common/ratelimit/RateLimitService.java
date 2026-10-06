package com.miniloja.common.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Rate limit simples em memória (janela fixa).
 *
 * <p>Observação: adequado para ambiente de desenvolvimento / instância única.
 * Para múltiplas instâncias, o ideal é backend distribuído (ex.: Redis).
 */
@Service
public class RateLimitService {

    private static final class Window {
        private Instant windowStart;
        private int count;

        private Window(Instant windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }

    private final Clock clock;
    private final Map<String, Window> counters = new ConcurrentHashMap<>();

    private final RateLimitRedisRepository redisRepository;

    /**
     * Construtor default para o Spring (injeção opcional de Redis).
     *
     * <p>Quando o starter do Redis estiver no classpath, o Spring criará o bean
     * {@link RateLimitRedisRepository} e este service usará Redis. Caso contrário, o
     * parâmetro virá null e o service fará fallback para memória.
     */
    @Autowired
    public RateLimitService(@org.springframework.lang.Nullable RateLimitRedisRepository redisRepository) {
        this.clock = Clock.systemUTC();
        this.redisRepository = redisRepository;
    }

    // útil para testes
    RateLimitService(Clock clock, RateLimitRedisRepository redisRepository) {
        this.clock = clock;
        this.redisRepository = redisRepository;
    }

    /**
     * Construtor para testes unitários sem Redis.
     */
    RateLimitService(Clock clock) {
        this(clock, null);
    }

    /**
     * Tenta consumir 1 requisição na janela.
     *
     * @return resultado indicando se foi permitido e quantos segundos faltam para liberar.
     */
    public RateLimitResult tryConsume(String key, int maxRequests, Duration window) {
        if (key == null || key.isBlank()) {
            // Sem chave, não aplicamos rate limit aqui (decisão defensiva).
            return RateLimitResult.allow();
        }

        if (redisRepository == null) {
            // Redis é obrigatório (dev/prod). Falhar de forma explícita evita divergência
            // doc x comportamento e previne bypass involuntário do rate limit.
            throw new IllegalStateException("Rate limit requer Redis, mas RateLimitRedisRepository não foi criado.");
        }

        var r = redisRepository.consume(key, window);
        if (r.current() > maxRequests) {
            long retryAfterSeconds = r.ttlSeconds() == null ? 1 : Math.max(1, r.ttlSeconds());
            return RateLimitResult.block(retryAfterSeconds);
        }
        return RateLimitResult.allow();
    }

    public record RateLimitResult(boolean allowed, Long retryAfterSeconds) {

        public static RateLimitResult allow() {
            return new RateLimitResult(true, null);
        }

        public static RateLimitResult block(long retryAfterSeconds) {
            return new RateLimitResult(false, retryAfterSeconds);
        }
    }
}
