package md.brainet.doeves.task;

import md.brainet.doeves.user.User;

import java.util.List;

public interface TaskDao {

    List<Task> selectAllTasksWhereUserIdIs(Integer userId);

    int insertTask(Task task);

    void update(Task task);

    void removeById(int taskId);

    Task selectById(int taskId);

    void updateStatusByTaskId(int taskId, boolean complete);
}
