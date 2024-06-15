package md.brainet.doeves.verification;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.user.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VerificationControllerIT extends IntegrationTestBase {

    private static final String VERIFIED_EMAIL = "test@mail.ru";
    private static final String UNVERIFIED_EMAIL = "unverified@mail.ru";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserDao userDao;

    @Autowired
    VerificationDetailsDao verificationDetailsDao;

    @Test
    void tryToVerify_receivedCodeIsCorrect_expect200() throws Exception {
        var details = fetchVerificationDetails(UNVERIFIED_EMAIL);

        tryToVerify(details)
                .andExpect(status().isOk());

        var user = userDao.selectUserByEmail(details.jwt().email());
        assertTrue(user.isPresent());
        assertTrue(user.get().isVerified());
    }

    @Test
    void thereAreAttemptsToVerify_receivedCodeIsNotCorrect_expect400BadCodeAndAttemptsDecrement() throws Exception {
        var details = fetchVerificationDetails(UNVERIFIED_EMAIL);

        tryToVerify(details.jwt, "231233")
                .andExpect(status().isBadRequest());

        var user = userDao.selectUserByEmail(details.jwt().email());
        assertTrue(user.isPresent());
        assertFalse(user.get().isVerified());
    }

    @Test
    void thereAreNotAttemptsToVerify_receivedCodeIsCorrect_expect410() throws Exception {
        var details = fetchVerificationDetails(UNVERIFIED_EMAIL);

        for(int i = 0; i < 5; i++) {
            tryToVerify(details.jwt, "111111")
                    .andExpect(status().isBadRequest());
        }

        tryToVerify(details)
                .andExpect(status().isGone());

        var user = userDao.selectUserByEmail(details.jwt().email());
        assertTrue(user.isPresent());
        assertFalse(user.get().isVerified());
    }

    @Test
    void tryToVerify_receivedCodeIsExpired_expect410Gone() throws Exception {

        String email = UNVERIFIED_EMAIL;

        var expiredDetails = VerificationDetailsFactory
                .build(new SixDigitsCodeGenerator());

        expiredDetails.setExpireDate(LocalDateTime.now().minusDays(12));

        verificationDetailsDao.updateVerificationDetails(email, expiredDetails);

        var details = fetchVerificationDetails(email);

        tryToVerify(details)
                .andExpect(status().isGone());

        var user = userDao.selectUserByEmail(details.jwt().email());
        assertTrue(user.isPresent());
        assertFalse(user.get().isVerified());
    }

    @Test
    void tryToVerify_userAlreadyVerified_expect400() throws Exception {
        var details = fetchVerificationDetails(VERIFIED_EMAIL);

        tryToVerify(details)
                .andExpect(status().isBadRequest());

        var user = userDao.selectUserByEmail(details.jwt().email());
        assertTrue(user.isPresent());
        assertTrue(user.get().isVerified());
    }

    @Test
    void sendNewVerificationCode_userAlreadyVerified_expect400() throws Exception {
        final String email = VERIFIED_EMAIL;

        String oldCode = requestCode(email);

        generateNewVerificationCode(email)
                .andExpect(status().isBadRequest());

        String newCode = requestCode(email);
        assertEquals(oldCode, newCode);
    }

    @Test
    void sendNewVerificationCode_userNotVerified_expect200() throws Exception {
        final String email = UNVERIFIED_EMAIL;

        String oldCode = requestCode(email);

        generateNewVerificationCode(email)
                .andExpect(status().isBadRequest());

        String newCode = requestCode(email);
        assertNotEquals(oldCode, newCode);
    }


    private ResultActions generateNewVerificationCode(String email) {
        var details = fetchVerificationDetails(email);
        try {
            return mockMvc.perform(
                    post("/api/v1/user/verification/new")
                            .header(HttpHeaders.AUTHORIZATION, details.jwt)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String requestCode(String email) {
        return verificationDetailsDao
                .selectVerificationDetailsByEmail(email)
                .get()
                .getCode();
    }

    private ResultActions tryToVerify(TestVerificationDetails details) {
        return tryToVerify(details.jwt, details.code);
    }

    private ResultActions tryToVerify(Jwt jwt, String code) {
        try {
            return mockMvc.perform(
                    post("/api/v1/user/verification?code=%s".formatted(code))
                            .header(HttpHeaders.AUTHORIZATION, jwt.value())
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private TestVerificationDetails fetchVerificationDetails(String email) {
        try {
            var user = userDao.selectUserByEmail(email).get();
            var token = mockMvc.perform(
                    post("/api/v1/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email" : "%s",
                                        "password" : "%s"
                                    }
                                    """.formatted(
                                            user.getEmail(),
                                            user.getPassword()
                            ))
            ).andReturn()
                    .getResponse()
                    .getHeader(HttpHeaders.AUTHORIZATION);
            var details = verificationDetailsDao
                    .selectVerificationDetailsByEmail(email);
            var jwt = new Jwt(token, email);
            return new TestVerificationDetails(details.get().getCode(), jwt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    record TestVerificationDetails(String code, Jwt jwt){}
    record Jwt(String value, String email) {}
}