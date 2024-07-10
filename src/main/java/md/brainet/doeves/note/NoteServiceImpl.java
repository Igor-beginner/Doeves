package md.brainet.doeves.note;

import md.brainet.doeves.exception.NoteNotFoundException;
import md.brainet.doeves.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteDao noteDao;

    public NoteServiceImpl(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    @Override
    public Note createNote(Integer ownerId, NoteDTO noteDTO) {
        if(Objects.isNull(noteDTO.orderNumber())) {
            noteDTO = new NoteDTO(
                    noteDTO.name(),
                    noteDTO.description(),
                    noteDTO.catalogId(),
                    0
            );
        }

        return noteDao.insertNote(ownerId, noteDTO)
                .orElseThrow(() -> new UserNotFoundException(ownerId));
    }

    @Override
    public void changeOrderNumber(Integer noteId, Integer orderNumber) {
        //todo we need somehow to get catalogID
        boolean updated = noteDao.updateOrderNumberByNoteId(noteId, orderNumber);
        if(!updated) {
            throw new NoteNotFoundException(noteId);
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
    public void removeNote(Integer noteId) {
        boolean removed = noteDao.removeByNoteId(noteId);
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
    public void changeCatalog(Integer noteId, Integer catalogId) {
        boolean updated = noteDao.updateCatalogIdForNote(catalogId, noteId);
        if(!updated) {
            throw new NoteNotFoundException(noteId);
        }
    }

    @Override
    public List<Note> fetchAllOwnerNote(Integer ownerId, LimitedListNoteRequest request) {
        List<Note> notes;
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