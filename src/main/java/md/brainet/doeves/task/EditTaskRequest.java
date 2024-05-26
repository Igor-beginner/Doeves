package md.brainet.doeves.task;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

public record EditTaskRequest(

        @NotBlank
        String name,

        String description,

        LocalDateTime dateOfDeadline
) {
}
