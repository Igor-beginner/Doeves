package md.brainet.doeves.verification;

public interface VerificationService {
    String verify(String email, String code);
    void generateNewCodeFor(String email);
}
