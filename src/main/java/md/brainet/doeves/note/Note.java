package md.brainet.doeves.note;

import java.time.LocalDateTime;
import java.util.List;

public record Note(
        Integer id,
        String name,
        String description,
        LocalDateTime dateOfCreate,
        Integer catalogId,
        Integer orderNumber
) {
}
