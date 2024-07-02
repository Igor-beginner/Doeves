package md.brainet.doeves.catalog;

import md.brainet.doeves.note.Note;

import java.util.List;
import java.util.Optional;

public interface CatalogDao {
    Catalog insertCatalog(CatalogDTO catalogDTO);
    List<Catalog> selectAllCatalogsByOwnerId(Integer ownerId, Integer offset, Integer limit);
    void updateOrderNumberByCatalogId(Integer catalogId, Integer orderNumber);
    void updateNameByCatalogId(Integer catalogId, String catalogName);
    boolean removeByCatalogId(Integer catalogId);
    List<Note> selectAllNotesByCatalogId(Integer catalogId, Integer offset, Integer limit);
}
