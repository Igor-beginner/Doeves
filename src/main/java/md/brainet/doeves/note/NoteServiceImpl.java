package md.brainet.doeves.note;

import md.brainet.doeves.exception.NoteNotFoundException;
import md.brainet.doeves.exception.UserNotFoundException;
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
    @Transactional
    public void changeOrderNumber(Integer editingNoteId, Integer frontNoteId, ViewContext context) {

        Note editingNote = fetchNote(editingNoteId);

        if(editingNote.catalogId() == null && context == ViewContext.CATALOG) {
            //todo throw NoteDoesNotHaveCatalogException
        }

        Integer frontNoteOrderNumber = frontNoteId == null
                ? 0
                : fetchOrderNumber(frontNoteId, context);

        boolean updated = noteDao.updateOrderNumberByNoteId(
                new NoteOrderingRequest(
                        editingNoteId,
                        fetchOrderNumber(editingNoteId, context),
                        frontNoteOrderNumber,
                        context,
                        context.getContextId(editingNote)
                )
        );

        if(!updated) {
            //todo NothingToUpdateException
        }
    }


    public int fetchOrderNumber(Integer noteId, ViewContext context) {
        return noteDao.selectOrderNumberByNoteIdAndContext(noteId, context)
                .orElseThrow(() -> new NoteNotFoundException(noteId));
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
        boolean updated = noteDao.moveNoteIdToNewCatalogId(catalogId, noteId);
        if(!updated) {
            throw new NoteNotFoundException(noteId);
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
