package md.brainet.doeves.exception;

public class VerificationCodeIsEmptyException extends RuntimeException {

    public VerificationCodeIsEmptyException() {
        super("Verification code is missing. Please generate this using /api/v1/user/verification/new");
    }
}
