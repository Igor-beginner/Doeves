package md.brainet.doeves.catalog;

import md.brainet.doeves.note.Note;

import java.util.List;

public interface CatalogService {
    Catalog createCatalog(CatalogDTO catalogDTO);
    Catalog findCatalog(Integer catalogId);
    List<Catalog> fetchAllOwnerCatalogs(Integer ownerId, Integer offset, Integer limit);
    void changeOrderNumber(Integer catalogId, Integer newOrderNumber);
    void removeCatalog(Integer id);
    List<Note> fetchAllCatalogNotes(Integer catalogId, Integer offset, Integer limit);
}
