package md.brainet.doeves.catalog;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CatalogPermissionUtil {

    private final CatalogService catalogService;

    public CatalogPermissionUtil(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public boolean haveEnoughRights(Integer catalogId, Integer ownerId) {
        if(catalogId == null) {
            return true;
        }
        var catalog = catalogService.findCatalog(catalogId);
        return catalog.ownerId().equals(ownerId);
    }

    public boolean haveEnoughRights(List<Integer> catalogsId, Integer ownerId) {
        if(catalogsId == null) {
            return true;
        }
        return catalogsId.stream()
                .allMatch(id ->
                        haveEnoughRights(id, ownerId)
                );
    }
}
