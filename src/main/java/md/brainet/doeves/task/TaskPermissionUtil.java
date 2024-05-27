package md.brainet.doeves.task;

import md.brainet.doeves.user.User;
import md.brainet.doeves.user.UserDao;
import org.springframework.stereotype.Component;

@Component
public class TaskPermissionUtil {

    private final UserDao userDao;

    public TaskPermissionUtil(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean haveEnoughRights(String email, Integer taskId) {
        var user = userDao.selectOwnerOfTaskWithId(taskId);
        return user.isPresent() && user.get().getEmail().equals(email);
    }
}
