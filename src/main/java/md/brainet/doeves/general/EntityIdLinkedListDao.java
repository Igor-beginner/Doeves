package md.brainet.doeves.general;

import java.util.List;

public interface EntityIdLinkedListDao<T> {
    Integer insertEntity(T entity);
    void removeEntity(Integer entityId);
    boolean removeEntity(Integer entityId, Integer contextId);
    void cleanUp();
    void updatePreviousEntityIdByContext(Integer previousEntityId, Integer currentEntityId, Integer contextId);
    Integer findFirstEntityIdByContext(Integer contextId);
    Integer findNextEntityIdByContext(Integer entityId, Integer contextId);
    Integer findPrevEntityIdByContext(Integer entityId, Integer contextId);
    List<Integer> findAllContextsForEntity(Integer entityId);
}
