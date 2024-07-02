package md.brainet.doeves.note;

import md.brainet.doeves.catalog.Catalog;
import md.brainet.doeves.catalog.CatalogDTO;

import java.util.List;

public interface NoteDao {
    Note insertNote(NoteDTO noteDTO);
    void updateOrderNumberByNoteId(Integer noteId, Integer orderNumber);
    void updateNameByNoteId(Integer noteId, String noteName);
    void updateDescriptionByNoteId(Integer noteId, String noteDescription);
    boolean removeByNoteId(Integer noteId);
}
