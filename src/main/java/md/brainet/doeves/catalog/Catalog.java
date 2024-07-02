package md.brainet.doeves.catalog;

import java.time.LocalDateTime;

public record Catalog(
        Integer id,
        String name,
        Integer orderNumber,
        Integer ownerId,
        LocalDateTime dateOfCreate
) {
}
