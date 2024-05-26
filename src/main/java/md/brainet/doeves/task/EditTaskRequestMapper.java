package md.brainet.doeves.task;

import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class EditTaskRequestMapper implements Function<EditTaskRequest, Task> {
    @Override
    public Task apply(EditTaskRequest editTaskRequest) {
        Task task = new Task();

        task.setName(editTaskRequest.name());

        task.setDescription(editTaskRequest.description());

        task.setDeadline(editTaskRequest.dateOfDeadline());

        return task;
    }
}
