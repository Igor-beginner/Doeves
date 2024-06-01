package md.brainet.doeves.exception;

import jakarta.servlet.http.HttpServletRequest;
import md.brainet.doeves.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@ControllerAdvice
public class DefaultErrorControllerAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultErrorControllerAdvice.class);

    @ExceptionHandler(EmailAlreadyExistsDaoException.class)
    public ResponseEntity<ApiError> handleException(
            HttpServletRequest request,
            EmailAlreadyExistsDaoException e
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                status.value(),
                LocalDateTime.now()
        );

        LOG.warn(
                "Someone[ip={}] tried to register under existing email: {}",
                request.getRemoteAddr(),
                e.getMessage()
        );
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiError> handleException(
            HttpServletRequest request,
            TaskNotFoundException e
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                status.value(),
                LocalDateTime.now()
        );

        LOG.warn(
                "Cannot find task: {}",
                e.getMessage()
        );

        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleException(
            HttpServletRequest request,
            UserNotFoundException e
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                status.value(),
                LocalDateTime.now()
        );

        LOG.warn(
                "Cannot find user: {}",
                e.getMessage()
        );
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleException(
            HttpServletRequest request,
            MethodArgumentNotValidException e
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        FieldError issuedField = e.getFieldErrors().get(0);
        String message = issuedField.getDefaultMessage();
        if(message != null) {
                message = message.formatted(issuedField.getRejectedValue());
        }

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                message,
                status.value(),
                LocalDateTime.now()
        );

        LOG.debug(
                "Cannot recognize data from [url='{}']: '{}'",
                request.getRequestURI(),
                message
        );
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleException(
            HttpServletRequest request,
            BadCredentialsException e
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                status.value(),
                LocalDateTime.now()
        );
        LOG.debug(
                "Someone[ip={}] tried to sign in, but it turned wrong on",
                request.getRemoteAddr()
        );
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ApiError> handleException(
            HttpServletRequest request,
            InsufficientAuthenticationException e
    ) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                "Unauthorized request. Permission denied.",
                status.value(),
                LocalDateTime.now()
        );

        LOG.warn(
                "Unknown user[ip={}] tried to access protected resources [url='{}']",
                request.getRemoteAddr(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleException(
            HttpServletRequest request,
            HttpMessageNotReadableException e,
            @AuthenticationPrincipal User user
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                status.value(),
                LocalDateTime.now()
        );
        LOG.debug(
                "User [email='{}'] sent unreadable data to[url='{}']",
                user.getEmail(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleException(
            HttpServletRequest request,
            AccessDeniedException e,
            @AuthenticationPrincipal User user
    ) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                status.value(),
                LocalDateTime.now()
        );

        LOG.warn(
                "User [email='{}'] tried to access strange resources [url='{}']",
                user.getEmail(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(VerificationBadCodeException.class)
    public ResponseEntity<ApiError> handleException(
            HttpServletRequest request,
            VerificationBadCodeException e
    ) {
        HttpStatus status = HttpStatus.EXPECTATION_FAILED;

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                status.value(),
                LocalDateTime.now()
        );

        LOG.warn(
                "Invalid code [code={}] for email [email={}]",
                e.getCode(),
                e.getEmail()
                );
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(
            HttpServletRequest request,
            Exception e,
            @AuthenticationPrincipal User user
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                status.value(),
                LocalDateTime.now()
        );

        LOG.error("Occuered an unknown exception  -> [uri=%s]"
                        .formatted(request.getRequestURI())
                , e);
        return new ResponseEntity<>(apiError, status);
    }
}
