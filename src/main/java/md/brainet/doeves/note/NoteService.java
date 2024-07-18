package md.brainet.doeves.note;

import md.brainet.doeves.catalog.CatalogOrderingRequest;
import md.brainet.doeves.user.User;

import java.util.List;

public interface NoteService {
    Note createNote(User user, NoteDTO noteDTO);
    void changeOrderNumber(Integer editingNoteId, Integer prevNoteId, Integer catalogId);
    void changeName(Integer noteId, String name);
    void changeDescription(Integer noteId, String description);
    void removeNote(Integer noteId, Integer catalogId);
    Note fetchNote(Integer noteId);
    void changeCatalog(CatalogOrderingRequest request);
    List<NotePreview> fetchAllOwnerNote(Integer ownerId, LimitedListNoteRequest request);
}
