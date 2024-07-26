package md.brainet.doeves.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import md.brainet.doeves.user.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class JWTUtil {

    private static final String IS_VERIFIED_FIELD_NAME = "isVerified";
    private static final String EXPIRED_DATE_FIELD_NAME = "exp";

    private final String secretKey;

    public JWTUtil(@Value("${jwt.secret.key}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String issueToken(String subject) {
        return issueToken(subject, Map.of());
    }

    public String issueToken(String subject, String ...scopes) {
        return issueToken(subject, Map.of("scopes", scopes));
    }

    public String issueTokenWithRoles(String subject, List<Role> scopes, boolean verified) {
        return issueToken(
                subject,
                scopes
                        .stream()
                        .map(Role::name)
                        .collect(Collectors.toList()),
                verified
        );
    }

    public String issueToken(String subject,
                             List<String> scopes,
                             boolean verified) {
        return issueToken(subject, Map.of(
                "scopes", scopes,
                IS_VERIFIED_FIELD_NAME, verified
        ));
    }


    public String issueToken(
            String subject,
            Map<String, Object> claims) {
        String token = Jwts
                .builder()
                .setClaims(claims)
                .setSubject(subject)
                //.setIssuer("https://amigoscode.com")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(
                        Date.from(
                                Instant.now().plus(15, DAYS)
                        )
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        return token;
    }

    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public boolean isTokenValid(String jwt, String username) {
        String subject = getSubject(jwt);
        return subject.equals(username) && !isTokenExpired(jwt);
    }

    public boolean isTokenVerified(String jwt) {
        return getClaims(jwt).get(IS_VERIFIED_FIELD_NAME, Boolean.class);
    }

    public LocalDateTime getExpireDate(String jwt) {
        return getClaims(jwt).get(EXPIRED_DATE_FIELD_NAME, LocalDateTime.class);
    }

    private boolean isTokenExpired(String jwt) {
        Date today = Date.from(Instant.now());
        return getClaims(jwt).getExpiration().before(today);
    }
}
