package md.brainet.doeves.security;

import md.brainet.doeves.exception.DelegatedAuthEntryPoint;
import md.brainet.doeves.jwt.JWTAuthenticationFilter;
import md.brainet.doeves.verification.VerificationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityFilterChainConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final VerificationFilter verificationFilter;
    private final DelegatedAuthEntryPoint delegatedAuthEntryPoint;

    public SecurityFilterChainConfig(
            AuthenticationProvider authenticationProvider,
            JWTAuthenticationFilter jwtAuthenticationFilter,
            DelegatedAuthEntryPoint delegatedAuthEntryPoint,
            VerificationFilter verificationFilter) {

        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.delegatedAuthEntryPoint = delegatedAuthEntryPoint;
        this.verificationFilter = verificationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request ->
                        request.requestMatchers(
                                HttpMethod.GET,
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()
                )
                .authorizeHttpRequests(request ->
                    request.requestMatchers(
                            HttpMethod.POST,
                            "/api/v1/user/login",
                            "/api/v1/user"
                    )
                            .permitAll()
                            .anyRequest()
                            .authenticated()
                )
                .sessionManagement(sessionManager ->
                    sessionManager
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(verificationFilter, JWTAuthenticationFilter.class)
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(delegatedAuthEntryPoint)
                );
        return http.build();
    }
}
