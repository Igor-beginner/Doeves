package md.brainet.doeves.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.NoSuchElementException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends NoSuchElementException {
    public UserNotFoundException(Integer userId) {
        this(userId, null);
    }

    public UserNotFoundException(Integer userId, Throwable throwable) {
        super(
                "User with id [%s] doesn't exist".formatted(userId),
                throwable
        );
    }
}
