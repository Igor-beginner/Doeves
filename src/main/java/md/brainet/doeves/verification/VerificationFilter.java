package md.brainet.doeves.verification;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import md.brainet.doeves.exception.VerificationException;
import md.brainet.doeves.jwt.JWTUtil;
import md.brainet.doeves.jwt.TokenAuthorizationHeaderPrefix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class VerificationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final TokenAuthorizationHeaderPrefix tokenAuthorizationHeaderPrefix;


    @Value("#{'${verification.uris}'.split(',')}")
    private List<String> verificationURIs;

    public VerificationFilter(JWTUtil jwtUtil,
                              TokenAuthorizationHeaderPrefix tokenAuthorizationHeaderPrefix) {
        this.jwtUtil = jwtUtil;
        this.tokenAuthorizationHeaderPrefix = tokenAuthorizationHeaderPrefix;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //TODO if user passed verification then we need return new jwt token
        // make addition condition and block verification for old token
        // or invalidate old token when verification was passed

        String rawToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        String requestURI = request.getRequestURI();

        String token = tokenAuthorizationHeaderPrefix.cleanFor(rawToken);
        boolean verified = jwtUtil.isTokenVerified(token);
        boolean contains = verificationURIs.contains(requestURI);

        if (!verified && !contains) {
            throw new VerificationException(null);
        }
        doFilter(request, response, filterChain);
    }
}
