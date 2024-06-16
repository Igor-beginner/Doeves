package md.brainet.doeves.verification;

import java.util.Optional;

public interface VerificationDetailsDao {

    void decrementVerificationDetailsAttempt(Integer id);

    Optional<VerificationDetails> decrementVerificationDetailsAttemptByEmail(String email);
    Optional<VerificationDetails> selectVerificationDetailsByEmail(String email);
    boolean updateVerificationDetails(String email, VerificationDetails details);
    Integer insertVerificationDetails(VerificationDetails details);

    boolean verifyUserByEmail(String email);
    boolean updateVerificationDetails(String email, Integer newVerificationDetailsId);

    boolean isUserVerified(String email);
}
