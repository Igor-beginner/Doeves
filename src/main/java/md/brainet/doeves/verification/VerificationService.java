package md.brainet.doeves.verification;

public interface VerificationService {
    void verify(String email, String code);
    void sendNewCodeTo(String email);

    Integer generateVerificationDetailsFor(String email);
}
