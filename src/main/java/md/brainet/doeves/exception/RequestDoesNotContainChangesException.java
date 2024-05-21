package md.brainet.doeves.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RequestDoesNotContainChangesException extends RuntimeException {

    public RequestDoesNotContainChangesException(String message) {
        super(message);
    }
}
