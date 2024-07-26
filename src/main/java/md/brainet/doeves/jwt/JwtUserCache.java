package md.brainet.doeves.jwt;

import java.time.LocalDateTime;

public interface JwtUserCache {
    void putJwtByUserIdUntil(Integer userId, String jwt, LocalDateTime expired);
    boolean exists(Integer userId);
}
