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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                    "password" : "73271737123"
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
                    "password" : "73271737123"
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
                content().json("""
                    {
                        "path" : "/api/v1/user/make",
                        "message" : "Email [test@mail.ru] already exists.",
                        "status_code" : 409,
                        "date" : "%s"
                    }
                """)
        );
    }

    @Test
    void makeNewUser_emailNotValid_expect400() throws Exception {
        //given
        var json = """
                    {
                        "email" : "testmail.ru",
                        "password" : "73271737123"
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
                content().json("""
                    {
                        "path" : "/api/v1/user/make",
                        "message" : "Email [testmail.ru] isn't valid.",
                        "status_code" : 400,
                        "date" : "%s"
                    }
                """)
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
                content().json("""
                    {
                        "path" : "/api/v1/user/make",
                        "message" : "Password isn't valid.",
                        "status_code" : 400,
                        "date" : "%s"
                    }
                """)
        );
    }
}