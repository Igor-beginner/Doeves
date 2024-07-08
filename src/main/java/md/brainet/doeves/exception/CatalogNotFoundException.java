package md.brainet.doeves.exception;

public class CatalogNotFoundException extends RuntimeException {

    private final int catalogId;

    public CatalogNotFoundException(int catalogId) {
        super("Catalog [id=%s] not found".formatted(catalogId));
        this.catalogId = catalogId;
    }

    public int getCatalogId() {
        return catalogId;
    }
}
