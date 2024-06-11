package md.brainet.doeves.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import md.brainet.doeves.auth.AuthenticationService;
import md.brainet.doeves.exception.InvalidTokenException;
import md.brainet.doeves.user.User;
import md.brainet.doeves.user.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter implements TokenAuthorizationHeaderPrefix {

    public static final String AUTHORIZATION_PREFIX = "pidr pizda token elda chlen ";

    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JWTAuthenticationFilter(JWTUtil jwtUtil,
                                   UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;

    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String jwt;
        try {
             jwt = cleanFor(authHeader);
        } catch (InvalidTokenException e) {
            doFilter(request, response, filterChain);
            return;
        }

        String subject = jwtUtil.getSubject(jwt);

        if (subject != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
            if (jwtUtil.isTokenValid(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                        );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);

    }

    @Override
    public String cleanFor(String token) {
        if (token == null || !token.startsWith(AUTHORIZATION_PREFIX)) {
            throw new InvalidTokenException("Token is not valid");
        }
        return token.substring(AUTHORIZATION_PREFIX.length());
    }
}
