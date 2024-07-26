package md.brainet.doeves.verification;

import md.brainet.doeves.jwt.JwtToken;

public interface VerificationService {
    String verify(String email, String code);
    void generateNewCodeFor(String email);
}
