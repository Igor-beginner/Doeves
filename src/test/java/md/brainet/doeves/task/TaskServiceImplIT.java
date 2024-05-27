package md.brainet.doeves.task;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.exception.TaskNotFoundException;
import md.brainet.doeves.exception.UserNotFoundException;
import md.brainet.doeves.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskServiceImplIT extends IntegrationTestBase {

    @Autowired
    TaskServiceImpl taskService;

    @Autowired
    TaskDao taskDao;

    @Test
    void fetchAllUserTasks_userExists_expectTasksList() {
        //given
        var userId = 2;

        //when
        var answer = taskService.fetchAllUserTasks(userId);

        //then
        assertEquals(1, answer.size());
    }

    @Test
    void fetchAllUserTasks_userNotExists_expectException() {
        //given
        var userId = 4324;

        //when
        Executable executable = () -> taskService.fetchAllUserTasks(userId);

        //then
        assertThrows(UserNotFoundException.class, executable);
    }

    @Test
    void makeTask_userExists_expectTaskId() {
        //given
        var userId = 2;
        var expectedTaskId = 4;
        var request = new NewTaskRequest(
                "Test1",
                null,
                null
        );

        //when
        var id = taskService.makeTask(userId, request);

        //then
        assertEquals(expectedTaskId, id);
    }

    @Test
    void makeTask_userNotExists_expectException() {
        //given
        var userId = 32;

        //when
        Executable executable = () -> taskService.makeTask(
                userId,
                new NewTaskRequest(
                        "sdfsdf",
                        "fsdfsd",
                        null
                )
        );

        //then
        assertThrows(UserNotFoundException.class, executable);
    }

    @Test
    void editTask_taskExists_expectChanges() {
        //given
        var taskId = 1;
        var oldTaskFromDB = taskDao.selectById(taskId).get();
        var request = new EditTaskRequest(
                "Task321",
                oldTaskFromDB.getDescription(),
                oldTaskFromDB.getDeadline()
        );

        //when
        taskService.editTask(taskId, request);

        //then
        var updatedTaskFromDB = taskDao.selectById(taskId).get();
        assertNotEquals(oldTaskFromDB.getName(), updatedTaskFromDB.getName());
    }

    @Test
    void editTask_taskNotExists_ExpectException() {
        //given
        var taskId = 3213;
        var request = new EditTaskRequest(
                "Task1",
                null,
                null
        );

        //when
        Executable executable = () -> taskService.editTask(taskId, request);

        //then
        assertThrows(TaskNotFoundException.class, executable);
    }

    @Test
    void deleteTask_taskExist_expectDeleting() {
        //given
        var taskId = 1;

        //when
        taskService.deleteTask(taskId);

        //then
        assertFalse(taskDao.selectById(taskId).isPresent());
    }

    @Test
    void deleteTask_taskNotExist_ExpectException() {
        //given
        var taskId = 323;

        //when
        Executable executable = () -> taskService.deleteTask(taskId);

        //then
        assertThrows(TaskNotFoundException.class, executable);
    }

    @Test
    void changeStatus_taskExist_expectUpdated() {
        //given
        var taskId = 2;
        var complete = false;

        //when
        taskService.changeStatus(taskId, complete);

        //then
        var task = taskDao.selectById(taskId).get();
        assertFalse(task.isComplete());
    }

    @Test
    void changeStatus_taskNotExist_expectException() {
        //given
        var taskId = 323;
        var complete = false;

        //when
        Executable executable = () -> taskService.changeStatus(taskId, complete);

        //then
        assertThrows(TaskNotFoundException.class, executable);
    }
}