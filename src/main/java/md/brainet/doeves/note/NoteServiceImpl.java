package md.brainet.doeves.note;

import md.brainet.doeves.catalog.CatalogOrderingRequest;
import md.brainet.doeves.exception.NoteNotFoundException;
import md.brainet.doeves.exception.NotesNotExistException;
import md.brainet.doeves.exception.UserNotFoundException;
import md.brainet.doeves.user.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteDao noteDao;

    public NoteServiceImpl(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    @Override
    public Note createNote(User user, NoteDTO noteDTO) {
        return noteDao.insertNote(user, noteDTO)
                .orElseThrow(() -> new UserNotFoundException(user.getId()));
    }

    @Override
    public void changeOrderNumber(Integer editingNoteId,
                                  Integer prevNoteId,
                                  Integer catalogId) {
        try {
            noteDao.updateOrderNumberByNoteId(prevNoteId, editingNoteId, catalogId);
        } catch (EmptyResultDataAccessException e) {
            throw new NoteNotFoundException(editingNoteId);
        }
    }
    @Override
    public void changeName(Integer noteId, String name) {
        boolean updated = noteDao.updateNameByNoteId(noteId, name);
        if(!updated) {
            throw new NoteNotFoundException(noteId);
        }
    }

    @Override
    public void changeDescription(Integer noteId, String description) {
        boolean updated = noteDao.updateDescriptionByNoteId(noteId, description);
        if(!updated) {
            throw new NoteNotFoundException(noteId);
        }
    }

    @Override
    @Transactional
    public void removeNotes(List<Integer> notesId, Integer catalogId) {
        List<Integer> notRemovedNotesId = new ArrayList<>();

        notesId.forEach(id -> {
                    boolean removed = noteDao.removeByNoteId(id, catalogId);
                    if (!removed) {
                        notRemovedNotesId.add(id);
                    }
                }
        );

        if(!notRemovedNotesId.isEmpty()) {
            throw new NotesNotExistException(notRemovedNotesId);
        }
    }

    @Override
    public Note fetchNote(Integer noteId) {
        return noteDao.selectByNoteId(noteId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));
    }

    @Override
    public void changeCatalog(CatalogOrderingRequest request) {
        if(request.getSourceCatalogId() == null) {
            request.setSourceCatalogId(request.getUser().getRootCatalogId());
        }
        noteDao.moveNoteIdToNewCatalogId(
                request.getNoteId(),
                request.getSourceCatalogId(),
                request.getDestinationCatalogId()
        );
    }

    @Override
    public List<NotePreview> fetchAllOwnerNote(Integer ownerId, LimitedListNoteRequest request) {
        List<NotePreview> notes;

        if (request.includingCatalogs()) {
            notes = noteDao.selectAllNotesByOwnerIdIncludingCatalogs(
                    ownerId,
                    request.offset(),
                    request.limit()
            );
        } else {
            notes = noteDao.selectAllNotesByOwnerIdWithoutCatalogs(
                    ownerId,
                    request.offset(),
                    request.limit()
            );
        }
        return notes;
    }
}
