package md.brainet.doeves.catalog;

import md.brainet.doeves.note.Note;
import org.springframework.data.relational.core.sql.In;

import java.util.List;

public interface CatalogService {
    Catalog createCatalog(Integer ownerId, CatalogDTO catalogDTO);
    Catalog findCatalog(Integer catalogId);
    List<Catalog> fetchAllOwnerCatalogs(Integer ownerId, Integer offset, Integer limit);
    Integer changeOrderNumber(Integer editingCatalogId, Integer backCatalogId);
    void removeCatalog(Integer id);
    void changeName(Integer catalogId, String newName);
    List<Note> fetchAllCatalogNotes(Integer catalogId, Integer offset, Integer limit);
}
