package md.brainet.doeves.exception;

public class VerificationCodeExpiredException extends VerificationBadCodeException {

    public VerificationCodeExpiredException(String email, String code) {
        super(email, code);
    }
}
