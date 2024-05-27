package md.brainet.doeves.task;

import md.brainet.doeves.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskPermissionUtilIT extends IntegrationTestBase {

    @Autowired
    TaskPermissionUtil taskPermissionUtil;

    @Test
    void haveEnoughRights_usersTask_expectThatPermitsHave() {
        //given
        var taskId = 1;
        var email = "test@mail.ru";

        //when
        boolean permit = taskPermissionUtil.haveEnoughRights(email, taskId);

        //then
        assertTrue(permit);
    }

    @Test
    void haveEnoughRights_notUsersTask_expectThatPermitsNotEnough() {
        //given
        var taskId = 4;
        var email = "test@mail.ru";

        //when
        boolean permit = taskPermissionUtil.haveEnoughRights(email, taskId);

        //then
        assertFalse(permit);
    }
}