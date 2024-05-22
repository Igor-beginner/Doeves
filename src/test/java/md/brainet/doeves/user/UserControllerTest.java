package md.brainet.doeves.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserService userService;

    @InjectMocks
    UserController userController;

    @Test
    void makeNewUser_expectCorrectIdAndMessage() {
        //given
        var generatedUserId = 412;
        NewUserRequest request = new NewUserRequest(
                "test@mail.ru",
                "password"
        );
        doReturn(generatedUserId)
                .when(userService)
                .makeUser(request);

        //when
        var response = userController.makeNewUser(request);

        //then
        assertNotNull(response);
        assertInstanceOf(NewUserResponse.class, response.getBody());
        assertEquals(
                generatedUserId,
                response.getBody().id()
        );
        assertEquals(
                "User was saved successfully",
                response.getBody().message()
        );
    }
}