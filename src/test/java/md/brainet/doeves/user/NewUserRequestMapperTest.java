package md.brainet.doeves.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.testcontainers.shaded.org.apache.commons.io.file.NoopPathVisitor;

import static org.junit.jupiter.api.Assertions.*;

class NewUserRequestMapperTest {

    NewUserRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NewUserRequestMapper(NoOpPasswordEncoder.getInstance());
    }

    @Test
    void apply() {
        //given
        var request = new NewUserRequest(
                "test@gmai.com",
                "123456test"
        );

        //when
        var user = mapper.apply(request);

        //then
        assertEquals(request.email(), user.getEmail());
        assertEquals(request.password(), user.getPassword());
    }
}