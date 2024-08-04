package md.brainet.doeves.general;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class EntityWithinContextLinkedListService<T> {

    private final EntityIdLinkedListDao<T> entitiesDao;


    public EntityWithinContextLinkedListService(EntityIdLinkedListDao<T> entitiesDao) {
        this.entitiesDao = entitiesDao;
    }

    @Transactional
    public Integer insertEntityIntoContextOnTop(T entity, Integer contextId) {
        return linkEntityToContext(
                () -> entitiesDao.insertEntity(entity),
                contextId
        );
    }

    @Transactional
    public void moveEntityBehind(Integer entityId, Integer movingEntityId, Integer contextId) {
        try {
            unlinkEntityFromContext(movingEntityId, contextId);
            linkEntityToContext(entityId, movingEntityId, contextId);
        } catch (IncorrectResultSizeDataAccessException ignore) {}
    }

    @Transactional
    public void deleteEntity(Integer entityId, Integer contextId) {
        boolean updated = entitiesDao.removeEntity(entityId, contextId);
        if(!updated) {
            throw new NoSuchElementException(
                    "Entity with id %s isn't within catalog id %s"
                            .formatted(entityId, contextId)
            );
        }
        entitiesDao.cleanUp();
    }

    @Transactional
    public void deleteEntity(List<Integer> entitiesId, Integer contextId) {
        entitiesId.forEach(id -> deleteEntity(id, contextId));
    }


    @Transactional
    public void deleteEntityAnywhere(List<Integer> entitiesId) {
        entitiesId.forEach(this::deleteEntityAnywhere);
    }

    @Transactional
    public void deleteEntityAnywhere(Integer entityId) {
        var contexts = entitiesDao.findAllContextsForEntity(entityId);
        contexts.forEach(contextId -> unlinkEntityFromContext(entityId, contextId));
        entitiesDao.removeEntity(entityId);
    }

    protected void unlinkEntityFromContext(Integer entityId, Integer contextId) {
        Integer nextNoteIdForRewritingNote = entitiesDao.findNextEntityIdByContext(entityId, contextId);
        Integer prevNoteIdForRewritingNote = entitiesDao.findPrevEntityIdByContext(entityId, contextId);
        if (nextNoteIdForRewritingNote != null) {
            entitiesDao.updatePreviousEntityIdByContext(prevNoteIdForRewritingNote, nextNoteIdForRewritingNote, contextId);
        }
    }

    protected void linkEntityToContext(Integer prevEntityId,
                                       Integer entityId,
                                       Integer contextId) {
        linkEntityToContext(
                prevEntityId,
                entityId,
                contextId,
                null,
                null
        );
    }

    protected Integer linkEntityToContext(AwayInsertRequest request,
                                          Integer contextId) {
        return linkEntityToContext(null, null, contextId, request, null);
    }

    protected void linkEntityToContext(Integer entityId,
                                       Integer contextId,
                                       AdditionalInsertRequest request) {
        linkEntityToContext(null, entityId, contextId, null, request);
    }


    private Integer linkEntityToContext(Integer prevEntityId,
                                          Integer entityId,
                                          Integer contextId,
                                          AwayInsertRequest awayInsertRequest,
                                          AdditionalInsertRequest additionalInsertRequest) {

        Integer nextId = findNextIdBehind(prevEntityId, contextId);

        if(Objects.nonNull(awayInsertRequest)) {
            entityId = awayInsertRequest.insert();
        }

        if(Objects.nonNull(additionalInsertRequest)) {
            additionalInsertRequest.insert(entityId, contextId);
        }

        linkDownIntoContext(prevEntityId, entityId, nextId, contextId);

        return entityId;
    }

    private Integer findNextIdBehind(Integer entityId, Integer contextId) {
        return entityId == null
                ? entitiesDao.findFirstEntityIdByContext(contextId)
                : entitiesDao.findNextEntityIdByContext(entityId, contextId);
    }

    private void linkDownIntoContext(Integer prevEntityId,
                                     Integer newEntityId,
                                     Integer nextEntityId,
                                     Integer contextId) {
        if(nextEntityId != null) {
            entitiesDao.updatePreviousEntityIdByContext(newEntityId, nextEntityId, contextId);
        }
        entitiesDao.updatePreviousEntityIdByContext(prevEntityId, newEntityId, contextId);
    }

    protected interface AwayInsertRequest {
        int insert();
    }

    protected interface AdditionalInsertRequest {
        void insert(Integer entityId, Integer contextId);
    }
}
