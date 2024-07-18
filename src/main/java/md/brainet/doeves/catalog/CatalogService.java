package md.brainet.doeves.catalog;

import md.brainet.doeves.note.NotePreview;

import java.util.List;

public interface CatalogService {
    Catalog createCatalog(Integer ownerId, CatalogDTO catalogDTO);
    Catalog findCatalog(Integer catalogId);
    List<Catalog> fetchAllOwnerCatalogs(Integer ownerId, Integer offset, Integer limit);
    void rewriteLinkAsPrevCatalogIdFor(Integer editingCatalogId, Integer backCatalogId);
    void removeCatalog(Integer id);
    void changeName(Integer catalogId, String newName);
    List<NotePreview> fetchAllCatalogNotes(Integer catalogId, Integer offset, Integer limit);
}
