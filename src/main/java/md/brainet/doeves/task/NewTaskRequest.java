package md.brainet.doeves.task;

import java.time.LocalDateTime;
import java.util.Date;

public record NewTaskRequest(
        String name,
        String description,
        LocalDateTime deadline
) {
}
