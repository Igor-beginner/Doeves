package md.brainet.doeves.verification;

import md.brainet.doeves.exception.VerificationBadCodeException;
import md.brainet.doeves.exception.VerificationCodeExpiredException;
import md.brainet.doeves.exception.VerificationCodeIsEmptyException;
import md.brainet.doeves.exception.VerificationException;
import md.brainet.doeves.mail.MailService;
import md.brainet.doeves.user.User;
import md.brainet.doeves.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    VerificationDetailsDao verificationDao;
    @Mock
    UserService userService;
    @Mock
    MailService mailService;
    @Mock
    CodeGenerator codeGenerator;

    @InjectMocks
    VerificationServiceImpl verificationService;

    String email;

    @BeforeEach
    void setUp() {
        this.email = "test123@mail.ru";
    }

    @Test
    void verify_userAlreadyVerified_expectVerificationException() {
        doReturn(true)
                .when(verificationDao).isUserVerified(email);

        Executable executable = () -> verificationService.verify(email, null);

        assertThrows(VerificationException.class, executable);
    }

    @Test
    void verify_userNotHaveVerificationCode_expectVerificationCodeIsEmptyException() {
        doReturn(false)
                .when(verificationDao).isUserVerified(email);
        doReturn(Optional.empty())
                .when(verificationDao).decrementVerificationDetailsAttemptByEmail(email);

        Executable executable = () -> verificationService.verify(email, null);

        assertThrows(VerificationCodeIsEmptyException.class, executable);
    }

    @Test
    void verify_tokenExpiredByDate_expectVerificationCodeExpiredException() {
        var details = VerificationDetailsFactory
                .build(new SixDigitsCodeGenerator());

        details.setExpireDate(LocalDateTime.now().minusDays(12));

        doReturn(false)
                .when(verificationDao).isUserVerified(email);
        doReturn(Optional.of(details))
                .when(verificationDao).decrementVerificationDetailsAttemptByEmail(email);

        Executable executable = () -> verificationService.verify(email, null);

        assertThrows(VerificationCodeExpiredException.class, executable);
    }

    @Test
    void verify_tokenExpiredByAttempts_expectVerificationCodeExpiredException() {
        var details = VerificationDetailsFactory
                .build(new SixDigitsCodeGenerator());

        details.setMissingAttempts(-1);

        doReturn(false)
                .when(verificationDao).isUserVerified(email);
        doReturn(Optional.of(details))
                .when(verificationDao).decrementVerificationDetailsAttemptByEmail(email);

        Executable executable = () -> verificationService.verify(email, null);

        assertThrows(VerificationCodeExpiredException.class, executable);
    }

    @Test
    void verify_codeIsNotCorrect_expectVerificationBadCodeException() {
        var details = VerificationDetailsFactory
                .build(new SixDigitsCodeGenerator());

        details.setCode("321322");

        doReturn(false)
                .when(verificationDao).isUserVerified(email);
        doReturn(Optional.of(details))
                .when(verificationDao).decrementVerificationDetailsAttemptByEmail(email);

        Executable executable = () -> verificationService.verify(email, "000000");

        assertThrows(VerificationBadCodeException.class, executable);
    }

    @Test
    void verify_codeIsCorrect_expectUserVerificationSuccess() {
        var details = VerificationDetailsFactory
                .build(new SixDigitsCodeGenerator());

        doReturn(false)
                .when(verificationDao).isUserVerified(email);
        doReturn(Optional.of(details))
                .when(verificationDao).decrementVerificationDetailsAttemptByEmail(email);

        verificationService.verify(email, details.getCode());

        verify(verificationDao).verifyUserByEmail(email);
    }

    @Test
    void generateNewCodeFor_emailExists_expectGeneratedCode() {
        User user = new User();
        user.setVerificationDetailsId(0);

        doReturn(user)
                .when(userService).findUser(email);

        verificationService.generateNewCodeFor(email);

        verify(verificationDao)
                .insertVerificationDetails(any(VerificationDetails.class));
        verify(verificationDao)
                .updateVerificationDetails(eq(email), any(Integer.class));
    }

    @Test
    void generateNewCodeFor_codeExists_expectRegeneratedCode() {
        User user = new User();
        user.setVerificationDetailsId(23);

        doReturn(user)
                .when(userService).findUser(email);

        verificationService.generateNewCodeFor(email);

        verify(verificationDao)
                .updateVerificationDetails(eq(email), any(VerificationDetails.class));
        verifyNoMoreInteractions(verificationDao);
    }
}