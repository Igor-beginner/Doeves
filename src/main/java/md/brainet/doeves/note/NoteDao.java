package md.brainet.doeves.note;

import md.brainet.doeves.catalog.Catalog;
import md.brainet.doeves.catalog.CatalogDTO;

import java.util.List;
import java.util.Optional;

public interface NoteDao {
    //TODO write test case when we try to insert note to busy position
    //TODO write test case when we try to insert note to null position (by default assign max + 1 order number)
    Optional<Note> insertNote(Integer ownerId, NoteDTO noteDTO);
    boolean updateOrderNumberByNoteId(Integer noteId, Integer orderNumber);
    boolean updateNameByNoteId(Integer noteId, String noteName);
    boolean updateDescriptionByNoteId(Integer noteId, String noteDescription);
    boolean removeByNoteId(Integer noteId);
    Optional<Note> selectByNoteId(Integer noteId);
    boolean updateCatalogId(Integer catalogId, Integer forNoteId);
    List<Note> selectAllNotesByOwnerIdIncludingCatalogs(Integer ownerId, Integer offset, Integer limit);
    List<Note> selectAllNotesByOwnerIdWithoutCatalogs(Integer ownerId, Integer offset, Integer limit);
}
