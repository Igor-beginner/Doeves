package md.brainet.doeves.task;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Date;

public record NewTaskRequest(
        @NotBlank(
                message = "Task name cannot be empty"
        )
        String name,
        String description,
        LocalDateTime deadline
) {
}
