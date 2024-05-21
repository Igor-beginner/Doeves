package md.brainet.doeves.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PrincipalNotFoundException extends RuntimeException {

    public PrincipalNotFoundException(String message) {
        super(message);
    }
}
