package md.brainet.doeves.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record Catalog(
        Integer id,
        String name,
        @JsonIgnore
        Integer prevCatalogId,
        @JsonIgnore
        Integer ownerId,
        @JsonProperty("date_of_create")
        LocalDateTime dateOfCreate
) {
}
