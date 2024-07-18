package md.brainet.doeves.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

public record Catalog(
        Integer id,
        String name,
        Integer prevCatalogId,
        @JsonIgnore
        Integer ownerId,
        LocalDateTime dateOfCreate
) {
}
