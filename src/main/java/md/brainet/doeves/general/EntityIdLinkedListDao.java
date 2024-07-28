package md.brainet.doeves.general;

public interface EntityIdLinkedListDao<T> {
    Integer insertEntity(T entity);
    void removeByEntityIdByContext(Integer entityId, Integer contextId);
    void updatePreviousEntityIdByContext(Integer previousEntityId, Integer currentEntityId, Integer contextId);
    Integer findFirstEntityIdByContext(Integer contextId);
    Integer findNextEntityIdByContext(Integer entityId, Integer contextId);
    Integer findPrevEntityIdByContext(Integer entityId, Integer contextId);
}
