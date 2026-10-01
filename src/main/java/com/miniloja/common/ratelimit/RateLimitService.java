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

        // Preferência: Redis (distribuído). Se indisponível, fallback para memória (dev).
        if (redisRepository != null) {
            try {
                var r = redisRepository.consume(key, window);
                if (r.current() > maxRequests) {
                    long retryAfterSeconds = r.ttlSeconds() == null ? 1 : Math.max(1, r.ttlSeconds());
                    return RateLimitResult.block(retryAfterSeconds);
                }
                return RateLimitResult.allow();
            } catch (RuntimeException e) {
                // Fallback para memória se Redis estiver indisponível.
            }
        }

        Instant now = clock.instant();

        Window w =
                counters.compute(
                        key,
                        (k, existing) -> {
                            if (existing == null) {
                                return new Window(now, 0);
                            }

                            if (Duration.between(existing.windowStart, now).compareTo(window) >= 0) {
                                existing.windowStart = now;
                                existing.count = 0;
                            }
                            return existing;
                        });

        synchronized (w) {
            // revalidar dentro do lock do objeto
            if (Duration.between(w.windowStart, now).compareTo(window) >= 0) {
                w.windowStart = now;
                w.count = 0;
            }

            if (w.count >= maxRequests) {
                long retryAfterSeconds = Math.max(1, window.minus(Duration.between(w.windowStart, now)).toSeconds());
                return RateLimitResult.block(retryAfterSeconds);
            }

            w.count++;
            return RateLimitResult.allow();
        }
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
