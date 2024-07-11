package md.brainet.doeves.catalog;

import md.brainet.doeves.note.Note;

import java.util.List;
import java.util.Optional;

public interface CatalogDao {
    Catalog insertCatalog(Integer ownerId, CatalogDTO catalogDTO);
    Optional<Catalog> selectCatalogById(Integer id);
    List<Catalog> selectAllCatalogsByOwnerId(Integer ownerId, Integer offset, Integer limit);
    boolean updateOrderNumberByCatalogId(CatalogOrderingRequest request);
    boolean updateNameByCatalogId(Integer catalogId, String catalogName);
    boolean removeByCatalogId(Integer catalogId);
    List<Note> selectAllNotesByCatalogId(Integer catalogId, Integer offset, Integer limit);
}
