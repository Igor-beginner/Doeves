package md.brainet.doeves.catalog;

import org.springframework.stereotype.Component;

@Component
public class CatalogPermissionUtil {

    private final CatalogService catalogService;

    public CatalogPermissionUtil(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public boolean haveEnoughRights(Integer catalogId, Integer ownerId) {
        var catalog = catalogService.findCatalog(catalogId);
        return catalog.ownerId().equals(ownerId) || catalogId == null;
    }
}
