package md.brainet.doeves.task;

import md.brainet.doeves.user.User;
import md.brainet.doeves.user.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskPermissionUtilTest {

    @Mock
    UserDao userDao;

    @InjectMocks
    TaskPermissionUtil taskPermissionUtil;

    @Test
    void haveEnoughRights_usersTask_expectThatPermitsHave() {
        //given
        var taskId = 1;
        var user = new User();
        user.setEmail("test@mail.ru");

        doReturn(Optional.of(user))
                .when(userDao).selectOwnerOfTaskWithId(taskId);

        //when
        boolean permit = taskPermissionUtil.haveEnoughRights(user.getEmail(), taskId);

        //then
        assertTrue(permit);
    }

    @Test
    void haveEnoughRights_notUsersTask_expectThatPermitsNotEnough() {
        //given
        var taskId = 1;
        var user = new User();
        user.setEmail("test@mail.ru");

        doReturn(Optional.of(user))
                .when(userDao).selectOwnerOfTaskWithId(taskId);

        //when
        boolean permit = taskPermissionUtil.haveEnoughRights("test@gmail.com", taskId);

        //then
        assertFalse(permit);
    }
}