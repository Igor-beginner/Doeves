package md.brainet.doeves.task;

import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class EditTaskRequestMapper implements Function<EditTaskRequest, Task> {
    @Override
    public Task apply(EditTaskRequest editTaskRequest) {
        Task task = new Task();

        var name = editTaskRequest.name();
        name.ifPresent(task::setName);

        var description = editTaskRequest.description();
        description.ifPresent(task::setDescription);

        var deadline = editTaskRequest.dateOfDeadline();
        deadline.ifPresent(task::setDeadline);

        return task;
    }
}
