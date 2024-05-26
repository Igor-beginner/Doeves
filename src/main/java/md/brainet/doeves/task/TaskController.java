package md.brainet.doeves.task;

import jakarta.validation.Valid;
import md.brainet.doeves.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("all")
    public ResponseEntity<?> fetchAll(@AuthenticationPrincipal User user) {
        List<Task> tasks = taskService.fetchAllUserTasks(user.getId());
        return ResponseEntity.ok(tasks);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<?> make(
            @Valid @RequestBody NewTaskRequest newTaskRequest,
            @AuthenticationPrincipal User user) {

        int taskId = taskService.makeTask(user.getId(), newTaskRequest);
        return new ResponseEntity<>(
                new TaskResponse(
                        "Task with id [%s] is created".formatted(taskId)
                ), HttpStatus.CREATED
        );
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("{id}")
    public ResponseEntity<?> edit(
            @PathVariable("id") int id,
            @Valid @RequestBody EditTaskRequest taskEditRequest) {

        taskService.editTask(id, taskEditRequest);
        return ResponseEntity.ok(
                new TaskResponse(
                        "Task with id [%s] is edited".formatted(id)
                )
        );
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public ResponseEntity<?> delete(@PathVariable("id") int taskId) {

        taskService.deleteTask(taskId);
        return ResponseEntity.ok(
                new TaskResponse(
                        "Task with id [%s] is deleted".formatted(taskId)
                )
        );
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("{id}/status")
    public ResponseEntity<?> changeStatus(
            @PathVariable("id") int taskId,
            @RequestParam Boolean complete) {

        taskService.changeStatus(taskId, complete);
        return ResponseEntity.ok(
                new TaskResponse(
                        "Task with id [%s] is "
                                .concat(complete
                                        ? "finished"
                                        : "uncompleted")
                )
        );
    }
}
