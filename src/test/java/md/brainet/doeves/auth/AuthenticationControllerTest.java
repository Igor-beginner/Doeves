package md.brainet.doeves.auth;

import md.brainet.doeves.user.Role;
import md.brainet.doeves.user.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    AuthenticationService authenticationService;

    @InjectMocks
    AuthenticationController authenticationController;

    @Test
    void login_tokenExists() {
        //given
        AuthenticationRequest request = new AuthenticationRequest(
                "test@gmail.com",
                "password123"
        );

        AuthenticationResponse authResponse = new AuthenticationResponse(
                "sdafsdafasdfasdfasd",
                new UserDTO(
                        123,
                        "test@gmail.com",
                        List.of(Role.USER)
                )
        );

        doReturn(authResponse).when(authenticationService).login(request);

        //when
        var response = authenticationController.login(request);

        //then
        assertNotNull(response);
        assertEquals(
                authResponse.token(),
                ((AuthenticationResponse)response.getBody())
                        .token()
        );
    }
}