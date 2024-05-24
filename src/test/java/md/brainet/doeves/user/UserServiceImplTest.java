package md.brainet.doeves.user;

import md.brainet.doeves.exception.EmailAlreadyExistsDaoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserDao userDao;

    @Mock
    NewUserRequestMapper mapper;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void makeUser_newUserNotExist_expectCorrectId() {
        //given
        Integer expectedId = 1;
        NewUserRequest request = new NewUserRequest(
                "email@mail.ru",
                "213123"
        );
        User user = new User();
        user.setId(expectedId);
        user.setEmail(request.email());
        user.setPassword(request.password());

        doReturn(user)
                .when(mapper)
                .apply(request);

        doReturn(user.getId())
                .when(userDao)
                .insertUserAndDefaultRole(user);

        //when
        Integer actualId = userService.makeUser(request);

        //then
        assertEquals(expectedId, actualId);
    }

    @Test
    void makeUser_newUserExist_expectException() {
        //given
        Integer expectedId = 1;
        NewUserRequest request = new NewUserRequest(
                "email@mail.ru",
                "213123"
        );
        User user = new User();
        user.setId(expectedId);
        user.setEmail(request.email());
        user.setPassword(request.password());

        doReturn(user)
                .when(mapper)
                .apply(request);

        doThrow(DuplicateKeyException.class)
                .when(userDao)
                .insertUserAndDefaultRole(user);

        //when
        Executable executable = () -> userService.makeUser(request);

        //then
        assertThrows(EmailAlreadyExistsDaoException.class, executable);
    }

    @Test
    void findUser_userExists_expectCorrectId() {
        //given
        var userId = 2;
        var user = new User();
        user.setId(userId);
        doReturn(Optional.of(user)).when(userDao).selectUserById(userId);

        //when
        var response = userService.findUser(userId);

        //then
        assertEquals(userId, response.getId());
    }

    @Test
    void findUser_userNotExists_ExpectNoSuchElementException() {
        //given
        var id = 3;
        doReturn(Optional.empty())
                .when(userDao).selectUserById(id);

        //when
        Executable response = () -> userService.findUser(3);

        //then
        assertThrows(NoSuchElementException.class, response);
    }
}