package md.brainet.doeves.user;

import md.brainet.doeves.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT extends IntegrationTestBase {

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
        mockMvc.perform(
                post("/api/v1/user/make")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
        //then
        ).andExpectAll(
                status().is(HttpStatus.CREATED.value())
        );
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
                post("/api/v1/user/make")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                //then
        ).andExpectAll(
                status().is(HttpStatus.CONFLICT.value()),
                jsonPath("$.path")
                        .value("/api/v1/user/make"),

                jsonPath("$.message")
                        .value("Email [test@mail.ru] already exists."),

                jsonPath("$.status_code")
                        .value(409),

                jsonPath("$.date")
                        .exists()
        );
    }

    @Test
    void makeNewUser_emailNotValid_expect400() throws Exception {
        //when
        mockMvc.perform(
                post("/api/v1/user/make")
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
                        .value("/api/v1/user/make"),

                jsonPath("$.message")
                        .value("Email [testmail.ru] isn't valid."),

                jsonPath("$.status_code")
                        .value(400),

                jsonPath("$.date")
                        .exists()
        );
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
                post("/api/v1/user/make")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                //then
        ).andExpectAll(
                status().is(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.path")
                        .value("/api/v1/user/make"),

                jsonPath("$.message")
                        .value("Password is not valid!"),

                jsonPath("$.status_code")
                        .value(400),

                jsonPath("$.date")
                        .exists()
        );
    }
}