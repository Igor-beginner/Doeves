package md.brainet.doeves.catalog;


import md.brainet.doeves.note.NotePreview;
import md.brainet.doeves.note.NotePreviewListResultSetMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
                RETURNING id, title, date_of_create;
                """;

        Integer firstNoteId = selectFirstCatalogIdByOwnerId(ownerId);

        var catalog =
                jdbcTemplate.query(
                        sql,
                        catalogMapper,
                        catalogDTO.name(),
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

    private Integer selectFirstCatalogIdByOwnerId(Integer ownerId) {
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

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                ownerId
        );
    }


    private void updatePrevIdAs(Integer prevId,
                                Integer catalogId) {
        var prevIdAsNSql = """
                UPDATE catalog
                SET prev_catalog_id = ?
                WHERE catalog_id = ?;
                """;

        jdbcTemplate.update(
                prevIdAsNSql,
                prevId,
                catalogId
        );
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
                    SELECT id, prev_catalog_id, owner_id, 0 as level
                    FROM catalog
                    WHERE prev_catalog_id IS NULL
                    AND owner_id = ?
                    UNION 
                    SELECT c.id, c.prev_catalog_id, owner_id, c.level + 1 as level
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
                   c.date_of_create
                FROM catalog c
                INNER JOIN catalogs_in_user_order ciuo
                ON ciuo.id = c.id
                AND ciuo.owner_id = c.owner_id
                ORDER BY ciuo.level
                OFFSET ?
                LIMIT ?;
                """;

        return jdbcTemplate.query(sql, catalogListMapper, ownerId, offset, limit);
    }

    @Override
    @Transactional
    public void updateOrderNumberByCatalogId(Integer prevCatalogId, Integer catalogId) {
        extractCatalogIdByRewritingLinks(prevCatalogId, catalogId);
        injectCatalogIdAsNextByRewritingLinksAfter(prevCatalogId, catalogId);
    }

    private void extractCatalogIdByRewritingLinks(Integer prevCatalogId,
                                                  Integer catalogId) {

        Integer nextCatalogIdForRewritingNote = findNextIdFor(catalogId);
        updatePrevIdAs(prevCatalogId, nextCatalogIdForRewritingNote);
    }

    private void injectCatalogIdAsNextByRewritingLinksAfter(Integer prevCatalogId,
                                                            Integer catalogId) {

        Integer nextId = findNextIdFor(catalogId);
        updatePrevIdAs(catalogId, nextId);
        updatePrevIdAs(prevCatalogId, catalogId);
    }

    private Integer findNextIdFor(Integer catalogId) {
        var findingNextCatalogIdSql = """
                SELECT id
                FROM catalog
                WHERE prev_id = ?;
                """;

        return jdbcTemplate.queryForObject(
                findingNextCatalogIdSql,
                Integer.class,
                catalogId
        );
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

    private Integer findPrevCatalogIdByCatalogId(Integer catalogId){
        var sql = """
                SELECT prev_id
                FROM catalog
                WHERE id = ?;
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                catalogId
        );
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
                    n.id,
                    n.title,
                    n.description,
                    n.date_of_create,
                    nfciuo.catalog_id,
                    NULL owner_id
                FROM notes_from_catalog_in_user_order nfciuo
                JOIN note n
                ON n.id = nfciuo.note_id
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
}
