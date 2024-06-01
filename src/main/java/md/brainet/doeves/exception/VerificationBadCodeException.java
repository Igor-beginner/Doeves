package md.brainet.doeves.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
public class VerificationBadCodeException extends RuntimeException {

    private final String email;

    private final String code;

    public VerificationBadCodeException(String email, String code) {
        super("Bad code.");

        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }
}
