package md.brainet.doeves.catalog;


import md.brainet.doeves.note.NotePreview;
import md.brainet.doeves.note.NotePreviewListResultSetMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcCatalogDao implements CatalogDao{

    private final JdbcTemplate jdbcTemplate;
    private final CatalogResultSetMapper catalogMapper;
    private final CatalogListResultSetMapper catalogListMapper;
    private final NotePreviewListResultSetMapper noteListMapper;

    public JdbcCatalogDao(JdbcTemplate jdbcTemplate,
                          CatalogResultSetMapper catalogMapper,
                          CatalogListResultSetMapper catalogListMapper,
                          NotePreviewListResultSetMapper noteListMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalogMapper = catalogMapper;
        this.catalogListMapper = catalogListMapper;
        this.noteListMapper = noteListMapper;
    }

    @Override
    @Transactional
    public Catalog insertCatalog(Integer ownerId, CatalogDTO catalogDTO) {
        var sql = """
                INSERT INTO catalog(title, prev_catalog_id, owner_id)
                VALUES(?, ?, ?)
                RETURNING id, title, date_of_create, prev_catalog_id, owner_id;
                """;

        Integer firstNoteId = selectFirstCatalogIdByOwnerId(ownerId);

        var catalog =
                jdbcTemplate.query(
                        sql,
                        catalogMapper,
                        catalogDTO.getName(),
                        null,
                        ownerId
                );

        if(catalog == null) {
            return catalog;
        }

        if(firstNoteId != null) {
            updatePrevIdAs(catalog.id(), firstNoteId);
        }

        return catalog;
    }

    @Override
    public Integer selectFirstCatalogIdByOwnerId(Integer ownerId) {
        var sql = """
                SELECT c.id
                FROM catalog c
                WHERE c.owner_id = ?
                AND c.prev_catalog_id IS NULL
                AND c.id != (
                    SELECT root_catalog_id
                    FROM users
                    WHERE id = c.owner_id
                );
                """;


        Integer firstCatalogId;

        try {
            firstCatalogId = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    ownerId
            );
        } catch (EmptyResultDataAccessException e) {
            firstCatalogId = null;
        }
        return firstCatalogId;
    }


    private void updatePrevIdAs(Integer prevId,
                                Integer catalogId) {
        var prevIdAsNSql = """
                UPDATE catalog
                SET prev_catalog_id = ?
                WHERE id = ?;
                """;

        if(catalogId != null) {
            jdbcTemplate.update(
                    prevIdAsNSql,
                    prevId,
                    catalogId
            );
        }
    }

    @Override
    public Optional<Catalog> selectCatalogById(Integer id) {
        var sql = """
                SELECT
                   id,
                   title,
                   owner_id,
                   prev_catalog_id,
                   date_of_create
                FROM catalog
                WHERE id = ?
                """;
        return Optional.ofNullable(
                jdbcTemplate.query(sql, catalogMapper, id)
        );
    }

    @Override
    public List<Catalog> selectAllCatalogsByOwnerId(Integer ownerId, Integer offset, Integer limit) {
        var sql = """
                WITH RECURSIVE catalogs_in_user_order AS (
                    SELECT id, prev_catalog_id, c.owner_id, 0 as level
                    FROM catalog c
                    WHERE prev_catalog_id IS NULL
                    AND c.owner_id = ?
                    UNION 
                    SELECT c.id, c.prev_catalog_id, c.owner_id, ciuo.level + 1 as level
                    FROM catalog c
                    JOIN catalogs_in_user_order ciuo
                    ON ciuo.id = c.prev_catalog_id
                    AND ciuo.owner_id = c.owner_id
                )
                
                SELECT
                   DISTINCT c.id,
                   c.title,
                   c.owner_id,
                   c.prev_catalog_id,
                   c.date_of_create,
                   ciuo.level
                FROM catalog c
                INNER JOIN catalogs_in_user_order ciuo
                ON ciuo.id = c.id
                AND ciuo.owner_id = c.owner_id
                INNER JOIN users u 
                ON u.id = c.owner_id
                WHERE u.root_catalog_id != c.id
                ORDER BY ciuo.level
                OFFSET ?
                LIMIT ?;
                """;

        return jdbcTemplate.query(sql, catalogListMapper, ownerId, offset, limit);
    }

    @Override
    @Transactional
    public void updateOrderNumberByCatalogId(Integer prevCatalogId, Integer catalogId) {
        extractCatalogIdByRewritingLinks(catalogId);
        injectCatalogIdAsNextByRewritingLinksAfter(prevCatalogId, catalogId);
    }

    private void extractCatalogIdByRewritingLinks(Integer catalogId) {

        Integer nextCatalogIdForRewritingNote = findNextIdFor(catalogId);
        Integer prevCatalogId = findPrevCatalogIdByCatalogId(catalogId);
        updatePrevIdAs(prevCatalogId, nextCatalogIdForRewritingNote);
    }

    private void injectCatalogIdAsNextByRewritingLinksAfter(Integer prevCatalogId,
                                                            Integer catalogId) {

        Integer nextId = prevCatalogId == null
                ? findFirstCatalogIdFromOwnerCatalogId(catalogId)
                : findNextIdFor(prevCatalogId);
        updatePrevIdAs(catalogId, nextId);
        updatePrevIdAs(prevCatalogId, catalogId);
    }

    private Integer findFirstCatalogIdFromOwnerCatalogId(Integer catalogId) {
        var sql = """
                SELECT id
                FROM catalog c
                WHERE prev_catalog_id IS NULL
                AND owner_id = (
                    SELECT owner_id
                    FROM catalog
                    WHERE id = ?
                )
                AND id != (
                    SELECT DISTINCT root_catalog_id
                    FROM users u
                    WHERE u.id = c.owner_id
                );
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                catalogId
        );
    }

    public Integer findNextIdFor(Integer catalogId) {
        var findingNextCatalogIdSql = """
                SELECT id
                FROM catalog
                WHERE prev_catalog_id = ?;
                """;

        Integer prevId;

        try {

            prevId = jdbcTemplate.queryForObject(
                    findingNextCatalogIdSql,
                    Integer.class,
                    catalogId
            );
        } catch (EmptyResultDataAccessException e) {
            prevId = null;
        }

        return prevId;
    }


    @Override
    public boolean updateNameByCatalogId(Integer catalogId, String catalogName) {
        var sql = """
                UPDATE catalog
                SET title = ?
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql, catalogName, catalogId) > 0;
    }

    @Override
    @Transactional
    public boolean removeByCatalogId(Integer catalogId) {
        var sql = """
                DELETE FROM catalog
                WHERE id = ?
                """;

        Integer prevId = findPrevCatalogIdByCatalogId(catalogId);
        Integer nextId = findNextIdFor(catalogId);

        updatePrevIdAs(prevId, nextId);

        return jdbcTemplate.update(sql, catalogId) > 0;
    }

    public Integer findPrevCatalogIdByCatalogId(Integer catalogId){
        var sql = """
                SELECT prev_catalog_id
                FROM catalog
                WHERE id = ?;
                """;

        Integer prevId;

        try {

            prevId = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    catalogId
            );
        } catch (EmptyResultDataAccessException e) {
            prevId = null;
        }

        return prevId;
    }

    @Override
    public List<Integer> selectAllNotesIdByCatalogId(Integer catalogId) {
        var sql = """
                SELECT note_id
                FROM note_catalog_ordering
                WHERE catalog_id = ?;
                """;

        return jdbcTemplate.queryForList(sql, Integer.class, catalogId);
    }

    @Override
    public List<NotePreview> selectAllNotesByCatalogId(Integer catalogId, Integer offset, Integer limit) {
        var sql = """
                WITH RECURSIVE notes_from_catalog_in_user_order AS (
                  SELECT note_id, catalog_id, 0 as level
                  FROM note_catalog_ordering
                  WHERE prev_note_id IS NULL
                  AND catalog_id = ?
                  UNION
                  SELECT nco.note_id, nco.catalog_id, nfciuo.level + 1 AS level
                  FROM note_catalog_ordering nco
                  JOIN notes_from_catalog_in_user_order nfciuo
                  ON nfciuo.note_id = nco.prev_note_id
                  AND nfciuo.catalog_id = nco.catalog_id
                )
                SELECT
                    n.id n_id,
                    n.title n_title,
                    n.description n_description,
                    n.date_of_create n_date_of_create,
                    c.id c_id,
                    c.title c_title,
                    u.root_catalog_id u_root_catalog_id
                FROM notes_from_catalog_in_user_order nfciuo
                JOIN note n
                ON n.id = nfciuo.note_id
                JOIN catalog c 
                ON c.id = nfciuo.catalog_id
                JOIN users u 
                ON u.id = c.owner_id
                ORDER BY nfciuo.level
                OFFSET ?
                LIMIT ?;
                """;

        return jdbcTemplate.query(
                sql,
                noteListMapper,
                catalogId,
                offset,
                limit
        );
    }


    @Override
    public Integer insertEntity(CatalogDTO entity) {
        var sql = """
                INSERT INTO catalog(title, prev_catalog_id, owner_id)
                VALUES(?, ?, ?)
                RETURNING id;
                """;

        return jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,
                entity.getName(),
                        null,
                        entity.getOwnerId()
        );
    }

    @Override
    public List<Integer> findAllContextsForEntity(Integer entityId) {
        var sql = """
                SELECT DISTINCT owner_id
                FROM catalog
                WHERE id = ?
                """;

        return jdbcTemplate.queryForList(sql, Integer.class, entityId);
    }

    @Override
    public void removeEntity(Integer entityId) {
        var sql = """
                DELETE FROM catalog
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, entityId);
    }

    @Override
    public boolean removeEntity(Integer entityId, Integer contextId) {
        var sql = """
                DELETE FROM catalog
                WHERE id = ?
                AND owner_id = ?
                """;

        return jdbcTemplate.update(sql, entityId, contextId) > 0;
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public void updatePreviousEntityIdByContext(Integer previousEntityId, Integer currentEntityId, Integer contextId) {
        var prevIdAsNSql = """
                UPDATE catalog
                SET prev_catalog_id = ?
                WHERE id = ?
                AND owner_id = ?;
                """;


        jdbcTemplate.update(prevIdAsNSql, previousEntityId, currentEntityId, contextId);
    }

    @Override
    public Integer findFirstEntityIdByContext(Integer contextId) {
        var sql = """
                SELECT id
                FROM catalog c
                WHERE prev_catalog_id IS NULL
                AND owner_id = ?
                AND id != (
                    SELECT DISTINCT root_catalog_id
                    FROM users u
                    WHERE u.id = c.owner_id
                );
                """;

        Integer entityId;
        try {
            entityId = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    contextId
            );
        } catch (EmptyResultDataAccessException e) {
            entityId = null;
        }

        return entityId;
    }
    @Override
    public Integer findNextEntityIdByContext(Integer entityId, Integer contextId) {
        var findingNextCatalogIdSql = """
                SELECT id
                FROM catalog
                WHERE prev_catalog_id = ?
                AND owner_id = ?;
                """;

        Integer prevId;

        try {

            prevId = jdbcTemplate.queryForObject(
                    findingNextCatalogIdSql,
                    Integer.class,
                    entityId,
                    contextId
            );
        } catch (EmptyResultDataAccessException e) {
            prevId = null;
        }

        return prevId;
    }

    @Override
    public Integer findPrevEntityIdByContext(Integer entityId, Integer contextId) {
        var sql = """
                SELECT prev_catalog_id
                FROM catalog
                WHERE id = ?
                AND owner_id = ?;
                """;

        Integer prevId;

        try {

            prevId = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    entityId,
                    contextId
            );
        } catch (EmptyResultDataAccessException e) {
            prevId = null;
        }

        return prevId;
    }
}
