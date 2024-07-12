package md.brainet.doeves.catalog;

import md.brainet.doeves.note.ViewContext;

public record CatalogOrderingRequest(
        Integer catalogId,
        Integer currentOrderNumber,
        Integer newOrderNumber,
        Integer ownerId
) {
}
