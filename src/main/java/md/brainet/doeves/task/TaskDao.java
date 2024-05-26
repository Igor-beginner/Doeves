package md.brainet.doeves.task;

import md.brainet.doeves.user.User;

import java.util.List;
import java.util.Optional;

public interface TaskDao {

    List<Task> selectAllTasksWhereUserIdIs(Integer userId);

    int insertTask(Task task);

    boolean update(Task task);

    boolean removeById(int taskId);

    Optional<Task> selectById(int taskId);

    boolean updateStatusByTaskId(int taskId, boolean complete);
    boolean userExists(int userId);
}
