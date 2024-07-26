package md.brainet.doeves.jwt;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisJwtCache implements JwtBlackListCache, JwtUserCache {

    private static final String JWT_BLACK_LIST_PREFIX = "jwtBlackList:";
    private static final String JWT_BLACK_LIST_STUB = "blackListed";

    private static final String JWT_USER_PREFIX = "jwtUser:";

    private final RedisTemplate<String, String> redisTemplate;
    private final JWTUtil jwtUtil;

    public RedisJwtCache(RedisTemplate<String, String> redisTemplate,
                         JWTUtil jwtUtil) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void putJwtUntilDateExpired(String jwt, LocalDateTime expireDate) {
        putExpire(JWT_BLACK_LIST_PREFIX + jwt, JWT_BLACK_LIST_STUB, expireDate);
    }

    @Override
    public void putJwtUntilDateExpired(String jwt) {
        var expireDate = jwtUtil.getExpireDate(jwt);
        putExpire(JWT_BLACK_LIST_PREFIX + jwt, JWT_BLACK_LIST_STUB, expireDate);
    }

    @Override
    public void putJwtByUserIdUntil(Integer userId, String jwt, LocalDateTime expireDate) {
        putExpire(JWT_USER_PREFIX + userId, jwt, expireDate);
    }

    @Override
    public boolean exists(String jwt) {
        return hasKey(JWT_BLACK_LIST_PREFIX, jwt);
    }


    @Override
    public boolean exists(Integer userId) {
        return hasKey(JWT_USER_PREFIX, userId);
    }

    private void putExpire(String key, String value, LocalDateTime expireDate) {
        Duration duration = durationFromNowUntil(expireDate);
        putExpire(key, value, duration);
    }

    private Duration durationFromNowUntil(LocalDateTime end) {
        return Duration.between(
                LocalDateTime.now(),
                end
        );
    }

    private void putExpire(String key, String value, Duration duration) {
        if(!duration.isNegative()) {
            redisTemplate.opsForValue().set(
                    key,
                    value,
                    duration.toSeconds(),
                    TimeUnit.SECONDS
            );
        }
    }

    private boolean hasKey(String prefix, Object object) {
        Boolean contains = redisTemplate.opsForValue()
                .getOperations()
                .hasKey(prefix + object);

        return Boolean.TRUE.equals(contains);
    }
}
