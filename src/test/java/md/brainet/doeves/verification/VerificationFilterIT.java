package md.brainet.doeves.verification;

import com.jayway.jsonpath.JsonPath;
import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.jwt.JWTAuthenticationFilter;
import md.brainet.doeves.user.User;
import md.brainet.doeves.user.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class VerificationFilterIT extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserDao userDao;

    @Test
    void verificationRoomTest_sendNotVerifiedTokenToProtectedEndpoint_expect401() throws Exception {
        //given
        User unverifiedUser = new User();
        unverifiedUser.setEmail("loba@mail.ru");
        unverifiedUser.setPassword("dfsfasdfsadf");
        userDao.insertUserAndDefaultRole(unverifiedUser);
        String unverifiedToken = mockMvc.perform(
                post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email" : "%s",
                                    "password" : "%s"
                                }
                                """.formatted(
                                        unverifiedUser.getEmail(),
                                        unverifiedUser.getPassword()
                                )
                        )
        )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.AUTHORIZATION);


        //when
        mockMvc.perform(
                get("/api/v1/task/all")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                JWTAuthenticationFilter.AUTHORIZATION_PREFIX
                                        .concat(unverifiedToken)
                        )
        //then
        ).andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    void verificationRoomTest_sendNotVerifiedTokenToVerificationEndpoint_expect200() throws Exception {
        String ver = mockMvc.perform(
                        post("/api/v1/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "email" : "test@mail.ru",
                                    "password" : "123456"
                                }
                                """
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.AUTHORIZATION);


        //when
        mockMvc.perform(
                get("/api/v1/task/all")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                JWTAuthenticationFilter.AUTHORIZATION_PREFIX
                                        .concat(ver)
                        )
                //then
        ).andExpect(
                status().isOk()
        );
    }

    @Test
    void verificationRoomTest_sendVerifiedTokenToVerificationEndpoint_expect400() throws Exception {
        String ver = mockMvc.perform(
                        post("/api/v1/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "email" : "test@mail.ru",
                                    "password" : "123456"
                                }
                                """
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.AUTHORIZATION);


        //when
        mockMvc.perform(
                post("/api/v1/user/verification/new")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                JWTAuthenticationFilter.AUTHORIZATION_PREFIX
                                        .concat(ver)
                        )
                //then
        ).andExpect(
                status().isBadRequest()
        );
    }

    @Test
    void verificationRoomTest_sendNotValidTokenToVerificationEndpoint_expect403() throws Exception {
        mockMvc.perform(
                post("/api/v1/user/verification/new")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "fdsfdfdsddfdsdsfdfd"
                        )
                //then
        ).andExpect(
                status().isForbidden()
        );
    }
}
