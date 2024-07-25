package md.brainet.doeves.verification;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import md.brainet.doeves.exception.InvalidTokenException;
import md.brainet.doeves.exception.UserAlreadyVerifiedException;
import md.brainet.doeves.exception.VerificationException;
import md.brainet.doeves.jwt.JWTUtil;
import md.brainet.doeves.jwt.TokenAuthorizationHeaderPrefix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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

        try {
            boolean contains = verificationURIs.contains(requestURI);
            String token = tokenAuthorizationHeaderPrefix.cleanFor(rawToken);
            boolean verified = jwtUtil.isTokenVerified(token);

            if (!contains && !verified) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().print("""
                        {
                            "message" : "%s"
                        }
                        """.formatted(
                                "Your account is not verified. Please confirm yourself."
                ));
                response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                return;
            } else if (contains && verified) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getWriter().print("""
                        {
                            "message" : "%s"
                        }
                        """.formatted(
                        "Your account already verified. Please - idi nahui."
                ));
                return;
            }
        } catch (InvalidTokenException e){
            //todo log
        } catch (MalformedJwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().print("""
                        {
                            "message" : "%s"
                        }
                        """.formatted(
                    "Token is not valid"
            ));
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            return;
        }
        doFilter(request, response, filterChain);

    }
}
