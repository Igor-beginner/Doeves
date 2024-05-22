package md.brainet.doeves.task;

import md.brainet.doeves.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    TaskService taskService;

    @InjectMocks
    TaskController taskController;

    static User USER;

    @BeforeAll
    static void init () {
        var userId = 3;
        USER = new User();
        USER.setId(userId);
    }

    @Test
    void fetchAll_ReturnTasks() {
        //given
        var tasks = List.of(
                new Task(),
                new Task(),
                new Task(),
                new Task()
        );

        doReturn(tasks)
                .when(taskService)
                .fetchAllUserTasks(USER.getId());

        //when
        var response = taskController.fetchAll(USER);

        //then
        assertNotNull(response);
        assertEquals(tasks, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void make_ReturnTaskId() {
        //given
        var taskId = 100;

        NewTaskRequest request = new NewTaskRequest(
                "Task",
                "Description here",
                LocalDateTime.of(
                        2024,
                        10,
                        20,
                        23,
                        0,
                        0
                )
        );

        doReturn(taskId)
                .when(taskService)
                .makeTask(
                        USER.getId(),
                        request
                );

        //when
        var response = taskController.make(
                request,
                USER
        );

        //then
        assertNotNull(response);
        assertInstanceOf(TaskResponse.class, response.getBody());
        assertEquals(
                new TaskResponse(
                        "Task with id [%s] is created"
                                .formatted(taskId)
                ).getMessage(),
                ((TaskResponse)response.getBody()).getMessage()
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    void edit() {
        //given
        var taskId = 30;
        EditTaskRequest request = new EditTaskRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        //when
        var response = taskController.edit(taskId, request);

        //then
        verify(taskService).editTask(taskId, request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(TaskResponse.class, response.getBody());
        assertEquals(
                new TaskResponse(
                        "Task with id [%s] is edited"
                                .formatted(taskId)
                ).getMessage(),
                ((TaskResponse)response.getBody()).getMessage()
        );
    }

    @Test
    void delete_expectCorrectMessage () {
        //given
        var taskId = 28391;

        //when
        var response = taskController.delete(taskId);

        //then
        verify(taskService).deleteTask(taskId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(TaskResponse.class, response.getBody());
        assertEquals(
                new TaskResponse(
                        "Task with id [%s] is deleted"
                                .formatted(taskId)
                ).getMessage(),
                ((TaskResponse)response.getBody()).getMessage()
        );
    }

    @Test
    void changeStatus_taskIsComplete_expectCorrectMessage () {
        //given
        var taskId = 21123;
        var complete = true;

        //when
        var response = taskController
                .changeStatus(taskId, complete);

        //then
        verify(taskService).changeStatus(taskId, complete);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(TaskResponse.class, response.getBody());
        assertEquals(
                new TaskResponse(
                        "Task with id [%s] is finished"
                                ).getMessage(),
                ((TaskResponse)response.getBody()).getMessage()
        );
    }

    @Test
    void changeStatus_taskIsNotComplete_expectCorrectMessage () {
        //given
        var taskId = 21123;
        var complete = false;

        //when
        var response = taskController
                .changeStatus(taskId, complete);

        //then
        verify(taskService).changeStatus(taskId, complete);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(TaskResponse.class, response.getBody());
        assertEquals(
                new TaskResponse(
                        "Task with id [%s] is uncompleted"
                ).getMessage(),
                ((TaskResponse)response.getBody()).getMessage()
        );
    }
}