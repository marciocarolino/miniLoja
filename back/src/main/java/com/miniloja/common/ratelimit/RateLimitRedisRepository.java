package com.miniloja.common.ratelimit;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Abstração mínima para Redis, para permitir testes unitários sem depender da interface
 * completa do Spring Data Redis.
 */
interface RedisRateLimitClient {
    Long incr(String key);

    Boolean expire(String key, Duration window);

    Long ttlSeconds(String key);
}

/**
 * Repositório Redis para rate limit (janela fixa), usando INCR + EXPIRE.
 *
 * <p>Chave: rl:{key}
 */
@Repository
public class RateLimitRedisRepository {

    private final RedisRateLimitClient redis;

    @Autowired
    public RateLimitRedisRepository(StringRedisTemplate redisTemplate) {
        this.redis =
                new RedisRateLimitClient() {
                    @Override
                    public Long incr(String key) {
                        return redisTemplate.opsForValue().increment(key);
                    }

                    @Override
                    public Boolean expire(String key, Duration window) {
                        return redisTemplate.expire(key, window);
                    }

                    @Override
                    public Long ttlSeconds(String key) {
                        return redisTemplate.getExpire(key);
                    }
                };
    }

    // útil para testes sem Redis
    RateLimitRedisRepository(RedisRateLimitClient redis) {
        this.redis = redis;
    }

    public RedisConsumeResult consume(String key, Duration window) {
        String redisKey = "rl:" + key;

        Long value = redis.incr(redisKey);
        if (value != null && value == 1L) {
            // primeira requisição dentro da janela: define expiração
            redis.expire(redisKey, window);
        }

        Long ttlSeconds = redis.ttlSeconds(redisKey);
        return new RedisConsumeResult(value == null ? 0L : value, ttlSeconds);
    }

    public record RedisConsumeResult(long current, Long ttlSeconds) {}
}
