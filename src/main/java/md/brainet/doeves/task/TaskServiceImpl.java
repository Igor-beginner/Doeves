package md.brainet.doeves.task;

import md.brainet.doeves.exception.TaskNotFoundException;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskDao taskDao;
    private final NewTaskRequestMapper taskMapper;
    private final EditTaskRequestMapper editTaskRequestMapper;

    public TaskServiceImpl(TaskDao taskDao,
                           NewTaskRequestMapper taskMapper,
                           EditTaskRequestMapper editTaskRequestMapper) {
        this.taskDao = taskDao;
        this.taskMapper = taskMapper;
        this.editTaskRequestMapper = editTaskRequestMapper;
    }

    @Override
    public List<Task> fetchAllUserTasks(Integer userId) {
        return taskDao.selectAllTasksWhereUserIdIs(userId);
    }

    @Override
    public int makeTask(
            Integer userId,
            NewTaskRequest newTaskRequest) {

        Task task = taskMapper.apply(newTaskRequest);
        task.setOwnerId(userId);
        return taskDao.insertTask(task);
    }

    @Override
    public void editTask(
            Integer taskId,
            EditTaskRequest taskEditRequest) {

        Task task = editTaskRequestMapper.apply(taskEditRequest);
        task.setId(taskId);

        boolean updated = taskDao.update(task);
        if (!updated) {
            throw new TaskNotFoundException(taskId);
        }
    }

    @Override
    public void deleteTask(Integer taskId) {
        boolean updated = taskDao.removeById(taskId);

        if(!updated) {
            throw new TaskNotFoundException(taskId);
        }
    }

    @Override
    public void changeStatus(
            Integer taskId,
            Boolean complete) {
        boolean updated = taskDao.updateStatusByTaskId(taskId, complete);

        if (!updated) {
            throw new TaskNotFoundException(taskId);
        }
    }
}
