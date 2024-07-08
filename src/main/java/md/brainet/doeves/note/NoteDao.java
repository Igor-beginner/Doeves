package md.brainet.doeves.note;

import java.util.List;
import java.util.Optional;

public interface NoteDao {
    Optional<Note> insertNote(Integer ownerId, NoteDTO noteDTO);
    boolean updateOrderNumberByNoteId(Integer noteId, Integer orderNumber);
    boolean updateNameByNoteId(Integer noteId, String noteName);
    boolean updateDescriptionByNoteId(Integer noteId, String noteDescription);
    boolean removeByNoteId(Integer noteId);
    Optional<Note> selectByNoteId(Integer noteId);
    boolean updateCatalogIdForNote(Integer catalogId, Integer forNoteId);
    List<Note> selectAllNotesByOwnerIdIncludingCatalogs(Integer ownerId, Integer offset, Integer limit);
    List<Note> selectAllNotesByOwnerIdWithoutCatalogs(Integer ownerId, Integer offset, Integer limit);
}
