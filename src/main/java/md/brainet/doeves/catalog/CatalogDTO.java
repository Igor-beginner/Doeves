package md.brainet.doeves.catalog;

import java.time.LocalDateTime;

public record CatalogDTO(
        String name,
        Integer orderNumber,
        Integer ownerId
) {
}
