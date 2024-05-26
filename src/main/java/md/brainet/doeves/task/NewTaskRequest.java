package md.brainet.doeves.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

public record NewTaskRequest(
        @NotBlank(
                message = "Task name cannot be empty"
        )
        String name,
        String description,
        LocalDateTime deadline
) {
}
