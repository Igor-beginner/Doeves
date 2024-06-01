package md.brainet.doeves.verification;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


public class VerificationDetailsFactory {

    private static final int VERIFICATION_LIFETIME_MINUTES = 10;
    private static final int VERIFICATION_ATTEMPTS = 5;
    public static VerificationDetails build(CodeGenerator codeGenerator) {
        VerificationDetails details = new VerificationDetails();
        details.setCode(codeGenerator.generate());
        details.setExpireDate(
                LocalDateTime
                        .now()
                        .plusMinutes(VERIFICATION_LIFETIME_MINUTES)
        );
        details.setMissingAttempts(VERIFICATION_ATTEMPTS);
        return details;
    }

}
