package md.brainet.doeves.task;

import md.brainet.doeves.user.User;

import java.util.List;

public interface TaskService {
    List<Task> fetchAllUserTasks(Integer userId);

    int makeTask(Integer userId, NewTaskRequest newTaskRequest);

    void editTask(Integer taskId, EditTaskRequest taskEditRequest);

    void deleteTask(Integer taskId);

    void changeStatus(Integer taskId, Boolean complete);
}
