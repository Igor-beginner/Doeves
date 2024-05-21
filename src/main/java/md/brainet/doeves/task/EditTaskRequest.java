package md.brainet.doeves.task;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

public record EditTaskRequest(
        Optional<String> name,
        Optional<String> description,
        Optional<LocalDateTime> dateOfDeadline
) {
}
