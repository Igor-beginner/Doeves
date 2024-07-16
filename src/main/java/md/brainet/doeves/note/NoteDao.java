package md.brainet.doeves.note;

import md.brainet.doeves.user.User;

import java.util.List;
import java.util.Optional;

public interface NoteDao {
    Optional<Note> insertNote(User user, NoteDTO noteDTO);
    boolean updateOrderNumberByNoteId(Integer prevNoteId, Integer noteId, Integer catalogId);
    boolean updateNameByNoteId(Integer noteId, String noteName);
    boolean updateDescriptionByNoteId(Integer noteId, String noteDescription);
    boolean removeByNoteId(Integer prevNoteId, Integer noteId, Integer catalogId);
    Optional<Note> selectByNoteId(Integer noteId);
    boolean moveNoteIdToNewCatalogId(Integer noteId, Integer currentCatalogId, Integer newCatalogId);
    List<NotePreview> selectAllNotesByOwnerIdIncludingCatalogs(Integer ownerId, Integer offset, Integer limit);
    List<NotePreview> selectAllNotesByOwnerIdWithoutCatalogs(Integer ownerId, Integer offset, Integer limit);
}
