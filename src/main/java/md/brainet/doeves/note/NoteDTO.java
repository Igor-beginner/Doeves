package md.brainet.doeves.note;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record NoteDTO(
        String name,
        String description,
        Integer catalogId,
        @JsonProperty(defaultValue = "0")
        Integer orderNumber
) {
}
