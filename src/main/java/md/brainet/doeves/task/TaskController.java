package md.brainet.doeves.task;

import jakarta.validation.Valid;
import md.brainet.doeves.auth.AuthenticationService;
import md.brainet.doeves.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/task")
public class TaskController {

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("all")
    public ResponseEntity<?> fetchAll(@AuthenticationPrincipal User user) {
        List<Task> tasks = taskService.fetchAllUserTasks(user.getId());
        LOG.info("Fetch all tasks request was performed and received from [{}]", user.getEmail());
        return ResponseEntity.ok(tasks);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<?> make(
            @Valid @RequestBody NewTaskRequest newTaskRequest,
            @AuthenticationPrincipal User user) {

        int taskId = taskService.makeTask(user.getId(), newTaskRequest);

        LOG.info("Make task[id={}] request was performed and received from [{}]", taskId, user.getEmail());
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
            @Valid @RequestBody EditTaskRequest taskEditRequest,
            @AuthenticationPrincipal User user) {

        taskService.editTask(id, taskEditRequest);
        LOG.info("Edit task[id={}] request was performed and received from [{}]", id, user.getEmail());
        return ResponseEntity.ok(
                new TaskResponse(
                        "Task with id [%s] is edited".formatted(id)
                )
        );
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public ResponseEntity<?> delete(
            @PathVariable("id") int taskId,
            @AuthenticationPrincipal User user) {

        taskService.deleteTask(taskId);
        LOG.info("Delete task[id={}] request was performed and received from [{}]", taskId, user.getEmail());
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
            @RequestParam Boolean complete,
            @AuthenticationPrincipal User user) {

        taskService.changeStatus(taskId, complete);
        LOG.info("Change status task[id={}] request was performed and received from [{}]", taskId, user.getEmail());
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
