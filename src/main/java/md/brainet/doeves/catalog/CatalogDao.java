package md.brainet.doeves.catalog;

import md.brainet.doeves.general.EntityIdLinkedListDao;
import md.brainet.doeves.note.Note;
import md.brainet.doeves.note.NotePreview;

import java.util.List;
import java.util.Optional;

public interface CatalogDao extends EntityIdLinkedListDao<CatalogDTO> {
    Catalog insertCatalog(Integer ownerId, CatalogDTO catalogDTO);
    Integer selectFirstCatalogIdByOwnerId(Integer ownerId);
    Optional<Catalog> selectCatalogById(Integer id);
    List<Catalog> selectAllCatalogsByOwnerId(Integer ownerId, Integer offset, Integer limit);
    void updateOrderNumberByCatalogId(Integer prevCatalogId, Integer catalogId);
    boolean updateNameByCatalogId(Integer catalogId, String catalogName);
    boolean removeByCatalogId(Integer catalogId);
    List<Integer> selectAllNotesIdByCatalogId(Integer catalogId);
    List<NotePreview> selectAllNotesByCatalogId(Integer catalogId, Integer offset, Integer limit);
}
