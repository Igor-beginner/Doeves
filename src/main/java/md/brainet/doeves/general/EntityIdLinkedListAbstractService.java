package md.brainet.doeves.general;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class EntityIdLinkedListAbstractService<T> {

    private final EntityIdLinkedListDao<T> entitiesDao;


    public EntityIdLinkedListAbstractService(EntityIdLinkedListDao<T> entitiesDao) {
        this.entitiesDao = entitiesDao;
    }

    @Transactional
    public void insertEntityIntoContextOnTop(T entity, Integer contextId) {
        Integer entityId = entitiesDao.insertEntity(entity);
        Integer firstNoteId = entitiesDao.findFirstEntityIdByContext(contextId);
        if(firstNoteId != null) {
            entitiesDao.updatePreviousEntityIdByContext(entityId, firstNoteId, contextId);
        }
    }

    @Transactional
    public void moveEntityBehind(Integer entityId, Integer movingEntityId, Integer contextId) {
        extractEntityByRewritingLinks(movingEntityId, contextId);
        injectNoteIdAsNextByRewritingLinksAfter(entityId, movingEntityId, contextId);
    }

    @Transactional
    public void deleteEntityFromContext(Integer entityId, Integer contextId) {
        Integer prevNoteId = entitiesDao.findPrevEntityIdByContext(entityId, contextId);

        Integer nextNoteIdOfDeletingNote = entitiesDao.findNextEntityIdByContext(entityId, contextId);

        if(nextNoteIdOfDeletingNote != null) {
            entitiesDao.updatePreviousEntityIdByContext(prevNoteId, nextNoteIdOfDeletingNote, contextId);
        }

        entitiesDao.removeByEntityIdByContext(entityId, contextId);
    }

    @Transactional
    public void deleteEntityFromContext(List<Integer> entitiesId, Integer contextId) {
        entitiesId.forEach(id -> deleteEntityFromContext(id, contextId));
    }

    private void extractEntityByRewritingLinks(Integer entityId, Integer contextId) {
        Integer nextNoteIdForRewritingNote = entitiesDao.findNextEntityIdByContext(entityId, contextId);
        Integer prevNoteIdForRewritingNote = entitiesDao.findPrevEntityIdByContext(entityId, contextId);
        entitiesDao.updatePreviousEntityIdByContext(prevNoteIdForRewritingNote, nextNoteIdForRewritingNote, contextId);
    }
    private void injectNoteIdAsNextByRewritingLinksAfter(Integer prevNoteId,
                                                         Integer noteId,
                                                         Integer catalogId) {

        Integer nextId = prevNoteId == null
                ? entitiesDao.findFirstEntityIdByContext(catalogId)
                : entitiesDao.findNextEntityIdByContext(prevNoteId, catalogId);
        if(nextId != null) {
            entitiesDao.updatePreviousEntityIdByContext(noteId, nextId, catalogId);
        }
        entitiesDao.updatePreviousEntityIdByContext(prevNoteId, noteId, catalogId);
    }
}
