package md.brainet.doeves.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.NoSuchElementException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskNotFoundException extends NoSuchElementException {
    public TaskNotFoundException(Integer userId) {
        super(
                "User with id [%s] doesn't exist".formatted(userId)
        );
    }
}
