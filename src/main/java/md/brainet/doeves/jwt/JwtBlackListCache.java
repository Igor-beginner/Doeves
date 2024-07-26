package md.brainet.doeves.jwt;

import java.time.LocalDateTime;

public interface JwtBlackListCache {
    void putJwtUntilDateExpired(String jwt, LocalDateTime expireDate);
    void putJwtUntilDateExpired(String jwt);
    boolean exists(String jwt);

}
