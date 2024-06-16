package md.brainet.doeves.verification;

public interface VerificationService {
    void verify(String email, String code);
    void generateNewCodeFor(String email);
}
