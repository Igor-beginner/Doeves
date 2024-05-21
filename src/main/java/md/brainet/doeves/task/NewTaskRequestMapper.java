package md.brainet.doeves.task;

import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class NewTaskRequestMapper implements Function<NewTaskRequest, Task> {

    @Override
    public Task apply(NewTaskRequest newTaskRequest) {
        Task task = new Task();
        task.setName(newTaskRequest.name());
        task.setDescription(newTaskRequest.description());
        task.setDeadline(newTaskRequest.deadline());
        return task;
    }
}
