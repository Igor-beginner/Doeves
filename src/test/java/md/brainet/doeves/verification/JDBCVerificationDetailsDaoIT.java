package md.brainet.doeves.verification;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.user.User;
import md.brainet.doeves.user.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JDBCVerificationDetailsDaoIT extends IntegrationTestBase {

    @Autowired
    JDBCVerificationDetailsDao verificationDao;

    @Autowired
    UserDao userDao;

    @Test
    void selectVerificationDetailsByEmail_userHaveVerificationDetails_expectIsPresent() {
        //given
        String email = "test@mail.ru";

        //when
        Optional<VerificationDetails> result = verificationDao
                .decrementVerificationDetailsAttemptByEmail(email);

        //then
        assertTrue(result.isPresent());
    }

    @Test
    void selectVerificationDetailsByEmail_userNotHaveVerificationDetails_expectIsNotPresent() {
        //given
        String email = "test1@mail.ru";

        User user = new User();
        user.setEmail(email);
        user.setPassword("213123123");

        userDao.insertUserAndDefaultRole(user);

        //when
        Optional<VerificationDetails> result = verificationDao
                .decrementVerificationDetailsAttemptByEmail(email);

        //then
        assertFalse(result.isPresent());
    }

    @Test
    void selectVerificationDetailsByEmail_userNotExist_expectIsPresent() {
        //given
        String email = "testsfds@mail.ru";

        //when
        Optional<VerificationDetails> result = verificationDao
                .decrementVerificationDetailsAttemptByEmail(email);

        //then
        assertFalse(result.isPresent());
    }

    @Test
    void updateVerificationDetails_userExists_expectUpdated() {
        //given
        String email = "test@mail.ru";
        VerificationDetails details = VerificationDetailsFactory
                .build(new SixDigitsCodeGenerator());

        //when
        boolean update = verificationDao.updateVerificationDetails(email, details);

        //then
        assertTrue(update);
    }

    @Test
    void updateVerificationDetails_userNotExists_expectNotUpdated() {
        //given
        String email = "testsdafas@mail.ru";
        VerificationDetails details = VerificationDetailsFactory
                .build(new SixDigitsCodeGenerator());

        //when
        boolean update = verificationDao.updateVerificationDetails(email, details);

        //then
        assertFalse(update);
    }

    @Test
    void insertVerificationDetails() {
        //given
        String email = "test123@mail.ru";
        VerificationDetails details = VerificationDetailsFactory
                .build(new SixDigitsCodeGenerator());

        User user = new User();
        user.setEmail(email);
        user.setPassword("fsdfsad");

        //when
        Integer verificationId = verificationDao.insertVerificationDetails(details);

        user.setVerificationDetailsId(verificationId);
        userDao.insertUserAndDefaultRole(user);

        //then
        Optional<VerificationDetails> detailsResult =
                verificationDao.decrementVerificationDetailsAttemptByEmail(email);

        assertTrue(detailsResult.isPresent());
        assertEquals(verificationId, detailsResult.get().getId());
    }
}