package md.brainet.doeves.user;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@WithMockUser(username = "Admin", roles = {"ADMIN"})
class UserPermissionUtilIT extends IntegrationTestBase {

    @Autowired
    UserPermissionUtil userPermissionUtil;

    @Autowired
    UserDao userDao;

    @Test
    void haveEnoughRightsOver_subUser_expectTrue() {;
        //given
        var user = userDao.selectUserById(1).get();

        //when
        boolean hasPermission = userPermissionUtil.haveEnoughRightsOver(user);

        //then
        assertTrue(hasPermission);
    }

    @Test
    void haveEnoughRightsOver_subUser_expectFalse() {
        //given
        var user = userDao.selectUserById(2).get();

        //when
        boolean hasPermission = userPermissionUtil.haveEnoughRightsOver(user);

        //then
        assertFalse(hasPermission);
    }

    @Test
    void haveEnoughRightsOver_userIdExists_expectTrue() {
        //given
        var userId = 1;

        //when
        boolean hasPermission = userPermissionUtil.haveEnoughRightsOver(userId);

        //then
        assertTrue(hasPermission);
    }

    @Test
    void haveEnoughRightsOver_userIdNotExists_expectException() {
        //given
        var userId = 12421;

        //when
        Executable executable = () -> userPermissionUtil.haveEnoughRightsOver(userId);

        //then
        assertThrows(UserNotFoundException.class, executable);
    }
}