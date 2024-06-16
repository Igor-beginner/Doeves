package md.brainet.doeves.user;

import md.brainet.doeves.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT extends IntegrationTestBase {

    @Autowired
    UserDao userDao;

    @Autowired
    MockMvc mockMvc;

    @Test
    void makeNewUser_emailNotExist_expect201() throws Exception {

        //given
        var json = """
                {
                    "email" : "valeratest@gmail.com",
                    "password" : "Qwerty123"
                }
                """;
        //when
        MvcResult result =  mockMvc.perform(
                post("/api/v1/user")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
        //then
        ).andExpectAll(
                status().is(HttpStatus.CREATED.value())
        ).andReturn();

        var user = userDao.selectUserById(4);
        assertEquals("valeratest@gmail.com", user.get().getEmail());
    }

    @Test
    void makeNewUser_emailExist_expect409() throws Exception {

        //given
        var json = """
                {
                    "email" : "test@mail.ru",
                    "password" : "Qwerty123"
                }
                """;
        //when
        mockMvc.perform(
                post("/api/v1/user")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                //then
        ).andExpectAll(
                status().is(HttpStatus.CONFLICT.value()),
                jsonPath("$.path")
                        .value("/api/v1/user"),

                jsonPath("$.content")
                        .value("Email [test@mail.ru] already exists."),

                jsonPath("$.status_code")
                        .value(409),

                jsonPath("$.date")
                        .exists()
        );

        var user = userDao.selectUserByEmail("test@mail.ru");
        assertTrue(user.isPresent());
    }

    @Test
    void makeNewUser_emailNotValid_expect400() throws Exception {
        //when
        mockMvc.perform(
                post("/api/v1/user")
                        .content("""
                                {
                                    "email" : "testmail.ru",
                                    "password" : "Qwerty123"
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON)
                //then
        ).andExpectAll(
                status().isBadRequest(),
                jsonPath("$.path")
                        .value("/api/v1/user"),

                jsonPath("$.content")
                        .value("Email [testmail.ru] isn't valid."),

                jsonPath("$.status_code")
                        .value(400),

                jsonPath("$.date")
                        .exists()
        );

        var user = userDao.selectUserByEmail("testmail.ru");
        assertFalse(user.isPresent());
    }

    @Test
    void makeNewUser_passwordNotValid_expect400() throws Exception {
        //given
        var json = """
                {
                    "email" : "aleratest@gmail.com",
                    "password" : "7аыфыsd"
                }
                """;
        //when
        mockMvc.perform(
                post("/api/v1/user")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                //then
        ).andExpectAll(
                status().is(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.path")
                        .value("/api/v1/user"),

                jsonPath("$.content")
                        .value("Password is not valid!"),

                jsonPath("$.status_code")
                        .value(400),

                jsonPath("$.date")
                        .exists()
        );

        var user = userDao.selectUserByEmail("aleratest@gmail.com");
        assertFalse(user.isPresent());
    }
}