package md.brainet.doeves.user;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.exception.EmailAlreadyExistsDaoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.jdbc.Sql;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class UserServiceImplIT extends IntegrationTestBase {

    @Autowired
    UserDao userDao;

    @Autowired
    UserServiceImpl userService;

    @Test
    void makeUser_newUserNotExist_expectCorrectId() {
        //given
        Integer expectedId = 3;
        NewUserRequest request = new NewUserRequest(
                "email@mail.ru",
                "213123"
        );

        //when
        Integer answer = userService.makeUser(request);

        //then
        assertEquals(expectedId, answer);
        Optional<User> user = userDao.selectUserById(expectedId);
        assertTrue(user.isPresent());
        assertEquals(request.email(), user.get().getEmail());
        assertEquals(request.password(), user.get().getPassword());
    }

    @Test
    void makeUser_newUserExist_expectException() {
        //given
        Integer expectedId = 1;
        NewUserRequest request = new NewUserRequest(
                "test@mail.ru",
                "213123"
        );

        //when
        Executable executable = () -> userService.makeUser(request);


        //then
        assertThrows(EmailAlreadyExistsDaoException.class, executable);
    }

    @Test
    void findUser_userExists_expectCorrectId() {
        //given
        var expectedId = 2;
        String expectedEmail = "testadmin@gmail.com";
        String expectedPassword = "admin";
        Role expectedRole = Role.ADMIN;

        //when
        var admin = userService.findUser(expectedId);

        //then
        assertEquals(expectedId, admin.getId());
        assertEquals(expectedEmail, admin.getEmail());
        assertEquals(expectedPassword, admin.getPassword());
        assertEquals(expectedRole, admin.getRole());
    }

    @Test
    void findUser_userNotExists_ExpectNoSuchElementException() {
        //given
        var id = 231;

        //when
        Executable executable = () -> userService.findUser(id);

        //then
        assertThrows(NoSuchElementException.class, executable);
    }
}