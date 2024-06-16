package md.brainet.doeves.verification;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VerificationServiceIT extends IntegrationTestBase {

    private static final String VERIFIED_EMAIL = "test@mail.ru";
    private static final String UNVERIFIED_EMAIL = "unverified@mail.ru";

    @Autowired
    VerificationService verificationService;

    @Autowired
    VerificationDetailsDao verificationDetailsDao;

    @Test
    void verify_userAlreadyVerified_expectVerificationException() {
        Executable executable = () -> verificationService.verify(VERIFIED_EMAIL, "312312");
        assertThrows(VerificationException.class, executable);
    }

    @Test
    void verify_userNotHaveVerificationCode_expectVerificationCodeIsEmptyException() {
        Executable executable = () -> verificationService.verify(UNVERIFIED_EMAIL, "312312");
        assertThrows(VerificationCodeIsEmptyException.class, executable);
    }

    @Test
    void verify_tokenExpiredByDate_expectVerificationCodeExpiredException() {
        VerificationDetails details = VerificationDetailsFactory.build(new SixDigitsCodeGenerator());
        details.setExpireDate(LocalDateTime.now().minusDays(12));
        verificationDetailsDao.insertVerificationDetails(details);
        verificationDetailsDao.updateVerificationDetails(UNVERIFIED_EMAIL, details);

        Executable executable = () -> verificationService.verify(UNVERIFIED_EMAIL, "312312");

        assertThrows(VerificationCodeExpiredException.class, executable);
    }

    @Test
    void verify_tokenExpiredByAttempts_expectVerificationCodeExpiredException() {
        VerificationDetails details = VerificationDetailsFactory.build(new SixDigitsCodeGenerator());
        details.setMissingAttempts(0);
        verificationDetailsDao.insertVerificationDetails(details);
        verificationDetailsDao.updateVerificationDetails(UNVERIFIED_EMAIL, details);

        Executable executable = () -> verificationService.verify(UNVERIFIED_EMAIL, "312312");

        assertThrows(VerificationCodeExpiredException.class, executable);
    }

    @Test
    void verify_codeIsNotCorrect_expectVerificationBadCodeException() {
        VerificationDetails details = VerificationDetailsFactory.build(new SixDigitsCodeGenerator());
        details.setMissingAttempts(0);
        verificationDetailsDao.insertVerificationDetails(details);
        verificationDetailsDao.updateVerificationDetails(UNVERIFIED_EMAIL, details);

        Executable executable = () -> verificationService.verify(UNVERIFIED_EMAIL, "312312");

        assertThrows(VerificationCodeExpiredException.class, executable);
    }

    @Test
    void verify_codeIsCorrect_expectUserVerificationSuccess() {
        VerificationDetails details = VerificationDetailsFactory.build(new SixDigitsCodeGenerator());
        details.setMissingAttempts(0);
        verificationDetailsDao.insertVerificationDetails(details);
        verificationDetailsDao.updateVerificationDetails(UNVERIFIED_EMAIL, details);

        verificationService.verify(UNVERIFIED_EMAIL, details.getCode());

        boolean isVerified = verificationDetailsDao.isUserVerified(UNVERIFIED_EMAIL);
        assertTrue(isVerified);
    }

    @Test
    void generateNewCodeFor_emailExists_expectGeneratedCode() {
        var oldCode = verificationDetailsDao.selectVerificationDetailsByEmail(UNVERIFIED_EMAIL);

        verificationService.generateNewCodeFor(UNVERIFIED_EMAIL);
        var generatedCode = verificationDetailsDao.selectVerificationDetailsByEmail(UNVERIFIED_EMAIL);

        assertFalse(oldCode.isPresent());
        assertTrue(generatedCode.isPresent());
    }

    @Test
    void generateNewCodeFor_codeExists_expectRegeneratedCode() {
        var oldCode = verificationDetailsDao.selectVerificationDetailsByEmail(UNVERIFIED_EMAIL);

        verificationService.generateNewCodeFor(UNVERIFIED_EMAIL);
        var generatedCode = verificationDetailsDao.selectVerificationDetailsByEmail(UNVERIFIED_EMAIL);

        assertTrue(oldCode.isPresent());
        assertTrue(generatedCode.isPresent());
        assertNotEquals(oldCode.get().getCode(), generatedCode.get().getCode());
    }

    @Test
    void generateNewCodeFor_emailNotExist_expectUserNotFoundException() {
        Executable executable = () -> verificationService.generateNewCodeFor("ddsfasd@mail.ru");

        assertThrows(UserNotFoundException.class, executable);
    }
}