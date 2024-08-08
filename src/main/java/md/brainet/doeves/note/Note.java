package md.brainet.doeves.note;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record Note(
        Integer id,
        String name,
        String description,
        LocalDateTime dateOfCreate
) {
}
