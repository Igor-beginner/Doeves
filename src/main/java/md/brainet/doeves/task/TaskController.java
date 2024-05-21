package md.brainet.doeves.task;

import md.brainet.doeves.user.User;
import org.springframework.http.HttpStatus;
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

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public ResponseEntity<?> make(
            @RequestBody NewTaskRequest newTaskRequest,
            @AuthenticationPrincipal User user) {

        int taskId = taskService.makeTask(user.getId(), newTaskRequest);
        return ResponseEntity.ok(
                new TaskResponse(
                        "Task with id [%s] is created".formatted(taskId)
                )
        );
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("{id}")
    public ResponseEntity<?> edit(
            @RequestParam int id,
            @RequestBody EditTaskRequest taskEditRequest) {

        taskService.editTask(id, taskEditRequest);
        return ResponseEntity.ok(
                new TaskResponse(
                        "Task with id [%s] is edited".formatted(id)
                )
        );
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("{id}")
    public ResponseEntity<?> delete(@RequestParam int taskId) {

        taskService.deleteTask(taskId);
        return ResponseEntity.ok(
                new TaskResponse(
                        "Task with id [%s] is deleted".formatted(taskId)
                )
        );
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("status/{id}")
    public ResponseEntity<?> changeStatus(
            @RequestParam int taskId,
            @RequestBody boolean complete) {

        taskService.changeStatus(taskId, complete);
        return ResponseEntity.ok(
                new TaskResponse(
                        "Task with id [%s] is "
                                .concat(complete
                                        ? "uncompleted again"
                                        : "finished")
                )
        );
    }
}
