package md.brainet.doeves.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    NewTaskRequestMapper taskMapper;

    @Mock
    EditTaskRequestMapper editTaskRequestMapper;

    @Mock
    TaskDao taskDao;

    @InjectMocks
    TaskServiceImpl taskService;

    @Test
    void fetchAllUserTasks_userExists_expectTasksList() {
        //given
        var tasks = List.of(
                new Task(),
                new Task(),
                new Task(),
                new Task(),
                new Task()
        );
        var userId = 1;
        doReturn(tasks)
                .when(taskDao).selectAllTasksWhereUserIdIs(userId);

        //when
        var answer = taskService.fetchAllUserTasks(userId);

        //then
        assertEquals(tasks, answer);
    }

    @Test
    void makeTask_userExists_ExpectTaskId() {
        //given
        var mockedTaskId = 3;
        var userId = 1;
        var request = new NewTaskRequest(
                "Test",
                "TDD",
                null
        );

        var task = new Task();
        task.setName(request.name());
        task.setDescription(request.description());
        task.setDeadline(request.deadline());

        doReturn(task)
                .when(taskMapper).apply(request);

        doReturn(mockedTaskId)
                .when(taskDao).insertTask(task);
        //when
        var taskId = taskService.makeTask(userId, request);

        //then
        assertEquals(mockedTaskId , taskId);
    }

    @Test
    void makeTask_userNotExists_expectException() {
        //given
        var userId = 231;
        var request = new NewTaskRequest(
                "3",
                "",
                null
        );
        var task = new Task();
        doReturn(task)
                .when(taskMapper).apply(request);

        doThrow(NoSuchElementException.class)
                .when(taskDao).insertTask(task);

        //when
        Executable executable = () -> taskService.makeTask(userId, request);

        //then
        assertThrows(NoSuchElementException.class, executable);
    }
    @Test
    void editTask_taskExists_invokeUpdateMethod() {
        //given
        var taskId = 3;
        var request = new EditTaskRequest(
                Optional.of("sda"),
                Optional.of("fdsasd"),
                Optional.empty()
        );
        var task = new Task();
        doReturn(task)
                .when(editTaskRequestMapper).apply(request);

        doReturn(true)
                .when(taskDao).update(task);

        //when
        taskService.editTask(taskId, request);

        //then
        verify(taskDao).update(task);
    }

    @Test
    void editTask_taskNotExists_ExpectException() {
        //given
        var taskId = 312;
        var request = new EditTaskRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.of(LocalDateTime.now())
        );
        var task = new Task();

        doReturn(task)
                .when(editTaskRequestMapper).apply(request);

        doReturn(false)
                .when(taskDao).update(task);

        //when
        Executable executable = () -> taskService.editTask(taskId, request);

        //then
        assertThrows(NoSuchElementException.class, executable);
    }

    @Test
    void deleteTask_taskExist_verifyMethod() {
        //given
        var taskId = 31;
        doReturn(true)
                .when(taskDao).removeById(taskId);

        //when
        taskService.deleteTask(taskId);

        //then
        verify(taskDao).removeById(taskId);

    }

    @Test
    void deleteTask_taskNotExist_ExpectException() {
        //given
        var taskId = 3132;
        doReturn(false)
                .when(taskDao).removeById(taskId);

        //when
        Executable executable = () -> taskService.deleteTask(taskId);

        //then
        assertThrows(NoSuchElementException.class, executable);

    }

    @Test
    void changeStatus_taskExist_expectUpdated() {
        //given
        var taskId = 312;
        var complete = true;

        doReturn(true)
                .when(taskDao).updateStatusByTaskId(taskId, complete);

        //when
        taskService.changeStatus(taskId, complete);

        //then
        verify(taskDao).updateStatusByTaskId(taskId, complete);
    }

    @Test
    void changeStatus_taskNotExist_expectException() {
        //given
        var taskId = 31243;
        var complete = false;

        doReturn(false)
                .when(taskDao).updateStatusByTaskId(taskId, complete);
        //when
        Executable executable = () -> taskService.changeStatus(taskId, complete);

        //then
        assertThrows(NoSuchElementException.class, executable);
    }
}