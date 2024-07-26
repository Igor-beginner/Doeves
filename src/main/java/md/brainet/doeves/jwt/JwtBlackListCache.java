package md.brainet.doeves.jwt;

import java.time.LocalDateTime;

public interface JwtBlackListCache {
    void putJwtUntil(String jwt, LocalDateTime expireDate);
    boolean exists(String jwt);

}
