package md.brainet.doeves.note;

import md.brainet.doeves.catalog.Catalog;
import md.brainet.doeves.catalog.CatalogDTO;

import java.util.List;
import java.util.Optional;

public interface NoteDao {
    Optional<Note> insertNote(NoteDTO noteDTO);
    boolean updateOrderNumberByNoteId(Integer noteId, Integer orderNumber);
    boolean updateNameByNoteId(Integer noteId, String noteName);
    boolean updateDescriptionByNoteId(Integer noteId, String noteDescription);
    boolean removeByNoteId(Integer noteId);

    Optional<Note> selectByNoteId(Integer noteId);
}
