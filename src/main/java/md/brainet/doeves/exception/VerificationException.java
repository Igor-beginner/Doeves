package md.brainet.doeves.exception;

public class VerificationException extends RuntimeException {

    private static final String REGENERATE_URI = "/api/v1/verify/code";

    public VerificationException(String email, String code) {

    }

    public VerificationException(String email) {

    }
}
