package md.brainet.doeves.auth;


import md.brainet.doeves.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityAuthenticationIT extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Test
    void permitOnLogin() throws Exception {
        mockMvc.perform(post("/api/v1/user/login"))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    void deniedOnBlockedEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/task/all"))
                .andExpectAll(
                        status().is(HttpStatus.FORBIDDEN.value()),
                        jsonPath("$.path")
                                .value("/api/v1/task/all"),

                        jsonPath("$.message")
                                .value("Unauthorized request. Permission denied."),

                        jsonPath("$.status_code")
                                .value(403),

                        jsonPath("$.date")
                                .exists()
                );
    }

    @Test
    void signIn_correctCredentials() throws Exception {
        mockMvc.perform(
                    post("/api/v1/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email" : "test@mail.ru",
                                        "password" : "123456"
                                    }
                                    """)
                ).andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.token").exists(),
                        header().exists(HttpHeaders.AUTHORIZATION)
                )
                .andReturn();
    }

    @Test
    void signIn_badCredentials() throws Exception {
        mockMvc.perform(
                post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "email" : "test@mail.ru",
                                        "password" : "12345632"
                                    }
                                    """)
        ).andExpectAll(
                status().is(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.path").value("/api/v1/user/login"),
                jsonPath("$.message").value("Bad credentials"),
                jsonPath("$.status_code").value(401),
                jsonPath("$.date").exists()
        );
    }
}
