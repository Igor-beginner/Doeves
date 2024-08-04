package md.brainet.doeves.note;

import md.brainet.doeves.catalog.CatalogOrderingRequest;
import md.brainet.doeves.exception.NoteNotFoundException;
import md.brainet.doeves.exception.NotesNotExistException;
import md.brainet.doeves.exception.UserNotFoundException;
import md.brainet.doeves.general.EntityWithinContextLinkedListService;
import md.brainet.doeves.user.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class NoteServiceImpl extends EntityWithinContextLinkedListService<NoteDTO> implements NoteService {

    private final NoteDao noteDao;

    public NoteServiceImpl(NoteDao noteDao) {
        super(noteDao);
        this.noteDao = noteDao;
    }

    public void deleteEntity(List<Integer> entitiesId, Integer catalogId, Integer rootCatalogId, boolean anywhere) {
        if (anywhere) {
            super.deleteEntityAnywhere(entitiesId);
        } else {
            super.deleteEntity(entitiesId, catalogId == null ? rootCatalogId : catalogId);
        }
    }
    @Transactional
    public Integer insertEntityIntoContextOnTop(NoteDTO entity, User user) {
        return insertEntityIntoContextOnTop(entity, user.getRootCatalogId());
    }

    @Override
    public Integer insertEntityIntoContextOnTop(NoteDTO entity, Integer rootCatalogId) {
        Integer entityId = noteDao.insertEntity(entity);

        super.linkEntityToContext(entityId, rootCatalogId, noteDao::insertIntoNoteCatalogOrdering);

        Integer catalogId = entity.getCatalogId();
        if(catalogId != null) {
            super.linkEntityToContext(entityId, catalogId, noteDao::insertIntoNoteCatalogOrdering);
        }
        return entityId;
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
            if(catalogId == null) {
                catalogId = ((User)SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal())
                        .getRootCatalogId();
            }
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

        Integer nullableCatalogId = catalogId == null ? getRootCatalogIdOfAuthenticatedUser() : catalogId;

        notesId.forEach(id -> {
                    boolean removed = noteDao.removeByNoteId(id, nullableCatalogId);
                    if (!removed) {
                        notRemovedNotesId.add(id);
                    }
                }
        );

        if(!notRemovedNotesId.isEmpty()) {
            throw new NotesNotExistException(notRemovedNotesId);
        }
    }

    private Integer getRootCatalogIdOfAuthenticatedUser() {
        return ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getRootCatalogId();
    }

    @Override
    public Note fetchNote(Integer noteId) {
        return noteDao.selectByNoteId(noteId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));
    }

    @Override
    @Transactional
    public void changeCatalog(CatalogOrderingRequest request) {
        if(request.getSourceCatalogId() == null) {
            request.setSourceCatalogId(request.getUser().getRootCatalogId());
        }
        unlinkEntityFromContext(request.getNoteId(), request.getSourceCatalogId());
        boolean updated = noteDao.removeFromNoteCatalogOrdering(request.getNoteId(), request.getSourceCatalogId());
        if(!updated) {
            throw new NoteNotFoundException(request.getNoteId());
        }
        AdditionalInsertRequest noteCatalogOrdering = noteDao::insertIntoNoteCatalogOrdering;
        linkEntityToContext(request.getNoteId(), request.getDestinationCatalogId(), noteCatalogOrdering);
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
