package md.brainet.doeves.note;

import md.brainet.doeves.catalog.CatalogOrderingRequest;
import md.brainet.doeves.exception.NoteNotFoundException;
import md.brainet.doeves.exception.UserNotFoundException;
import md.brainet.doeves.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (noteDTO.getCatalogId() == null) {
            noteDTO.setCatalogId(user.getRootCatalogId());
        }
        return noteDao.insertNote(user, noteDTO)
                .orElseThrow(() -> new UserNotFoundException(user.getId()));
    }

    @Override
    public void changeOrderNumber(Integer editingNoteId,
                                  Integer prevNoteId,
                                  Integer catalogId) {
        noteDao.updateOrderNumberByNoteId(prevNoteId, editingNoteId, catalogId);
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
    public void removeNote(Integer noteId, Integer catalogId) {
        boolean removed = noteDao.removeByNoteId(noteId, catalogId);
        if(!removed) {
            throw new NoteNotFoundException(noteId);
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

        boolean updated = noteDao.moveNoteIdToNewCatalogId(
                request.getNoteId(),
                request.getSourceCatalogId(),
                request.getDestinationCatalogId()
        );
        if(!updated) {
            throw new NoteNotFoundException(request.getNoteId());
        }
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
