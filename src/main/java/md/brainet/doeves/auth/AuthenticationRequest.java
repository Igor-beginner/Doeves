package md.brainet.doeves.auth;

public record AuthenticationRequest(
        String email,
        String password
) {
}
