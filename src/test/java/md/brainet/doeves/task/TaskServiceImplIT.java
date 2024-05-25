package md.brainet.doeves.task;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.exception.RequestDoesNotContainChangesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
        assertTrue(answer.isEmpty());
    }

    @Test
    void fetchAllUserTasks_userNotExists_expectException() {
        //given
        var userId = 4324;

        //when
        Executable executable = () -> taskService.fetchAllUserTasks(userId);

        //then
        assertThrows(NoSuchElementException.class, executable);
    }

    @Test
    void makeTask_userExists_expectTaskId() {
        //given
        var userId = 3;
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
        assertThrows(NoSuchElementException.class, executable);
    }

    @Test
    void editTask_taskExists_expectChanges() {
        //given
        var taskId = 1;
        var oldTaskFromDB = taskDao.selectById(taskId).get();
        var request = new EditTaskRequest(
                Optional.of("Task1"),
                Optional.of(oldTaskFromDB.getDescription()),
                Optional.ofNullable(oldTaskFromDB.getDeadline())
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
                Optional.of("Task1"),
                Optional.empty(),
                Optional.empty()
        );

        //when
        Executable executable = () -> taskService.editTask(taskId, request);

        //then
        assertThrows(NoSuchElementException.class, executable);
    }

    @Test
    void editTask_taskHaveNotChanged_ExpectException() {
        //given
        var taskId = 2;
        var task = taskDao.selectById(taskId).get();
        var request = new EditTaskRequest(
                Optional.of(task.getName()),
                Optional.of(task.getDescription()),
                Optional.of(task.getDeadline())
        );

        //when
        Executable executable = () -> taskService.editTask(taskId, request);

        //then
        assertThrows(RequestDoesNotContainChangesException.class, executable);
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
        assertThrows(NoSuchElementException.class, executable);
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
        assertThrows(NoSuchElementException.class, executable);
    }
}