package com.miniloja.common.ratelimit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.miniloja.common.ratelimit.RateLimitService.RateLimitResult;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class RateLimitServiceTest {

    private static RateLimitRedisRepository redisStubRepo(Clock clock) {
        // Fake mínimo de Redis com INCR/EXPIRE/TTL em memória,
        // suficiente para validar a regra de negócio sem depender de Redis real no unit test.
        class State {
            long current = 0L;
            Instant expiresAt = null;
        }

        java.util.Map<String, State> store = new java.util.concurrent.ConcurrentHashMap<>();

        RedisRateLimitClient stub =
                new RedisRateLimitClient() {
                    @Override
                    public Long incr(String key) {
                        State s = store.computeIfAbsent(key, k -> new State());
                        Instant now = clock.instant();

                        if (s.expiresAt != null && now.isAfter(s.expiresAt)) {
                            // janela expirada: reset
                            s.current = 0L;
                            s.expiresAt = null;
                        }

                        s.current++;
                        return s.current;
                    }

                    @Override
                    public Boolean expire(String key, Duration window) {
                        State s = store.computeIfAbsent(key, k -> new State());
                        s.expiresAt = clock.instant().plus(window);
                        return true;
                    }

                    @Override
                    public Long ttlSeconds(String key) {
                        State s = store.get(key);
                        if (s == null || s.expiresAt == null) {
                            return -1L;
                        }
                        long seconds = Duration.between(clock.instant(), s.expiresAt).toSeconds();
                        return Math.max(0L, seconds);
                    }
                };

        return new RateLimitRedisRepository(stub);
    }

    @Test
    void shouldAllowUpToLimitThenBlockWithinSameWindow_loginByIp() {
        Clock fixed = Clock.fixed(Instant.parse("2026-09-23T12:00:00Z"), ZoneOffset.UTC);
        RateLimitService service = new RateLimitService(fixed, redisStubRepo(fixed));

        Duration window = Duration.ofMinutes(1);

        for (int i = 0; i < 10; i++) {
            RateLimitResult r = service.tryConsume("ip:127.0.0.1", 10, window);
            assertTrue(r.allowed(), "Requisição " + (i + 1) + " deveria ser permitida");
        }

        RateLimitResult blocked = service.tryConsume("ip:127.0.0.1", 10, window);
        assertFalse(blocked.allowed(), "A 11ª requisição deveria ser bloqueada");
        assertNotNull(blocked.retryAfterSeconds(), "Deve retornar Retry-After");
    }

    @Test
    void shouldResetAfterWindowExpires() {
        Instant t0 = Instant.parse("2026-09-23T12:00:00Z");

        class MutableClock extends Clock {
            private Instant instant = t0;

            void plusSeconds(long seconds) {
                instant = instant.plusSeconds(seconds);
            }

            @Override
            public ZoneOffset getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(java.time.ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return instant;
            }
        }

        MutableClock clock = new MutableClock();
        RateLimitService service = new RateLimitService(clock, redisStubRepo(clock));

        Duration window = Duration.ofMinutes(1);

        // estoura o limite 2
        assertTrue(service.tryConsume("user:a@a.com", 2, window).allowed());
        assertTrue(service.tryConsume("user:a@a.com", 2, window).allowed());
        assertFalse(service.tryConsume("user:a@a.com", 2, window).allowed());

        // avança tempo para próxima janela
        clock.plusSeconds(61);

        assertTrue(service.tryConsume("user:a@a.com", 2, window).allowed(), "Após janela expirar deve permitir novamente");
    }
}
