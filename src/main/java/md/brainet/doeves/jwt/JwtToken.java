package md.brainet.doeves.jwt;

import java.time.LocalDateTime;

public record JwtToken(
        String token,
        LocalDateTime expired
) {
}
