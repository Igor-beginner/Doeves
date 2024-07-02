package md.brainet.doeves.note;

import java.time.LocalDateTime;

public record NoteDTO(
        String name,
        String description,
        Integer catalogId,
        Integer orderNumber
) {
}
