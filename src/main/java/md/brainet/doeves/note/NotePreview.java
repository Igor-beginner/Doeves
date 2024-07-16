package md.brainet.doeves.note;

import com.fasterxml.jackson.annotation.JsonProperty;
import md.brainet.doeves.catalog.CatalogPreview;

import java.time.LocalDateTime;

public record NotePreview(
        Integer id,
        String name,
        String description,
        CatalogPreview catalog,
        LocalDateTime dateOfCreate
) {
}
