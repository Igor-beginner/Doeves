package md.brainet.doeves.note;

import java.util.List;

public interface NoteService {
    Note createNote(Integer ownerId, NoteDTO noteDTO);
    void changeOrderNumber(Integer noteId, Integer orderNumber);
    void changeName(Integer noteId, String name);
    void changeDescription(Integer noteId, String description);
    void removeNote(Integer noteId);
    Note fetchNote(Integer noteId);
    void changeCatalog(Integer noteId, Integer catalogId);
    List<Note> fetchAllOwnerNote(Integer ownerId, LimitedListNoteRequest request);
}
