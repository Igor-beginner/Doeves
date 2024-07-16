package md.brainet.doeves.catalog;

public record CatalogOrderingRequest(
        Integer catalogId,
        Integer currentOrderNumber,
        Integer newOrderNumber,
        Integer ownerId
) {
}
