package md.brainet.doeves.verification;

import java.util.Optional;

public interface VerificationDetailsDao {

    Optional<VerificationDetails> selectVerificationDetailsByEmail(String email);
    Optional<String> selectVerificationCodeByEmail(String email);
    boolean updateVerificationDetails(String email, VerificationDetails details);
    Integer insertVerificationDetails(VerificationDetails details);
}
