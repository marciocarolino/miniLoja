package com.miniloja.common.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

/**
 * Teste unitário com stub (não depende de Redis rodando).
 *
 * <p>Valida que o repositório chama INCR e EXPIRE quando o contador vira 1.
 */
class RateLimitRedisRepositoryTest {

    @Test
    void shouldExpireOnFirstIncrement() {
        class StubClient implements RedisRateLimitClient {
            long incrementValue = 0;
            boolean expireCalled = false;

            @Override
            public Long incr(String key) {
                incrementValue++;
                return incrementValue;
            }

            @Override
            public Boolean expire(String key, Duration window) {
                expireCalled = true;
                return true;
            }

            @Override
            public Long ttlSeconds(String key) {
                return 60L;
            }
        }

        StubClient stub = new StubClient();
        RateLimitRedisRepository repo = new RateLimitRedisRepository(stub);

        RateLimitRedisRepository.RedisConsumeResult r1 = repo.consume("user:test@example.com", Duration.ofMinutes(1));
        assertEquals(1L, r1.current());
        assertTrue(stub.expireCalled, "expire deve ser chamado na primeira requisição");
    }
}
