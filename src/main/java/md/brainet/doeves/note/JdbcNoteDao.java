package md.brainet.doeves.note;

import md.brainet.doeves.exception.NoteNotFoundException;
import md.brainet.doeves.user.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class JdbcNoteDao implements NoteDao {

    private final JdbcTemplate jdbcTemplate;
    private final NoteResultSetMapper noteMapper;
    private final NoteListResultSetMapper noteListMapper;
    private final NotePreviewListResultSetMapper notePreviewListMapper;

    public JdbcNoteDao(JdbcTemplate jdbcTemplate,
                       NoteResultSetMapper noteMapper,
                       NoteListResultSetMapper noteListMapper,
                       NotePreviewListResultSetMapper notePreviewListMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.noteMapper = noteMapper;
        this.noteListMapper = noteListMapper;
        this.notePreviewListMapper = notePreviewListMapper;
    }

    @Override
    @Transactional
    public Optional<Note> insertNote(User user, NoteDTO noteDTO) {
        var sql = """
                INSERT INTO note(title, description)
                VALUES(?, ?)
                RETURNING *;
                """;

        var note = Optional.ofNullable(
                jdbcTemplate.query(
                        sql,
                        noteMapper,
                        noteDTO.getName(),
                        noteDTO.getDescription()
                )
        );

        if(note.isEmpty()) {
            return note;
        }

        Note concreteNote = note.get();

        int noteId = concreteNote.id();

        if(noteDTO.getCatalogId() != null) {
            insertIntoNoteCatalogOrderingByRewritingLinks(noteId, noteDTO.getCatalogId());
        }
        insertIntoNoteCatalogOrderingByRewritingLinks(noteId, user.getRootCatalogId());
        return note;
    }

    @Override
    @Transactional
    public boolean updateOrderNumberByNoteId(Integer prevNoteId, Integer noteId, Integer catalogId) {
        extractNoteIdByRewritingLinks(noteId, catalogId);
        injectNoteIdAsNextByRewritingLinksAfter(prevNoteId, noteId, catalogId);
        return true;
    }

    @Override
    public boolean updateNameByNoteId(Integer noteId, String noteName) {
        var sql = """
                UPDATE note
                SET title = ?
                WHERE id = ?;
                """;

        return jdbcTemplate.update(sql, noteName, noteId) > 0;
    }

    @Override
    public boolean updateDescriptionByNoteId(Integer noteId, String noteDescription) {
        var sql = """
                UPDATE note
                SET description = ?
                WHERE id = ?;
                """;

        return jdbcTemplate.update(sql, noteDescription, noteId) > 0;
    }

    @Override
    @Transactional
    public boolean removeByNoteId(Integer noteId, Integer catalogId) {
        var sql = """
                DELETE FROM note_catalog_ordering
                WHERE note_id = ?
                AND catalog_id = ?;
                """;

        Integer prevNoteId = findPrevIdFor(noteId, catalogId);

        Integer nextNoteIdOfDeletingNote = findNextIdFor(noteId, catalogId);

        if(nextNoteIdOfDeletingNote != null) {
            updatePrevIdAs(prevNoteId, nextNoteIdOfDeletingNote, catalogId);
        }

        return jdbcTemplate.update(sql, noteId, catalogId) > 0;
    }

    public Integer findPrevIdFor(Integer noteId, Integer catalogId) {
        var sql = """
                SELECT prev_note_id
                FROM note_catalog_ordering nco
                WHERE note_id = ?
                AND catalog_id = ?;
                """;
        Integer prevId;
        try {
            prevId = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    noteId,
                    catalogId
            );
        } catch (EmptyResultDataAccessException e) {
            prevId = null;
        }

        return prevId;
    }

    @Override
    public Optional<Note> selectByNoteId(Integer noteId) {
        var sql = """
                WITH RECURSIVE content_of_global_container AS (
                    SELECT
                        c.id,
                        c.prev_id,
                        c.content_type,
                        c.container_id,
                        c.value,
                        c.date_of_create,
                        1 AS level
                    FROM content c
                    WHERE prev_id IS NULL
                    AND container_id = (
                        SELECT global_container_id
                        FROM note
                        WHERE id = ?
                    )
                    UNION
                    SELECT c.id,
                        c.prev_id,
                        c.content_type,
                        c.container_id,
                        c.value,
                        c.date_of_create,
                        cogc.level + 1 AS level
                    FROM content c
                    JOIN content_of_global_container cogc
                    ON c.id = cogc.prev_id
                    AND c.container_id = cogc.container_id
                )
                SELECT
                    n.id ni,
                    n.title nt,
                    n.description nd,
                    n.date_of_create ndoc,
                    cogc.id ci,
                    cogc.content_type cc,
                    cogc.value cv,
                    cogc.date_of_create cdoc,
                    c.id c_id,
                    container_content.id cci,
                    container_content.content_type ccct,
                    container_content.value ccv,
                    container_content.date_of_create ccd,
                    t.id ti,
                    t.name tn,
                    t.description td,
                    t.deadline tdl,
                    t.date_of_create tdoc,
                    t.is_complete tic
                FROM note n
                LEFT JOIN content_of_global_container cogc
                ON n.global_container_id = cogc.id
                LEFT JOIN container c
                ON cogc.id = c.id
                AND cogc.id != n.global_container_id
                AND cogc.content_type = 'container'
                LEFT JOIN content container_content
                ON container_content.container_id = c.id
                LEFT JOIN task t
                ON (
                    CAST(cogc.value AS INTEGER) = t.id OR CAST(container_content.value AS INTEGER) = t.id
                )
                AND cogc.content_type = 'task'
                ORDER BY cogc.level;
                """;

        return Optional.ofNullable(
                jdbcTemplate.query(
                        sql, noteMapper, noteId
                )
        );
    }

    @Override
    @Transactional
    public void moveNoteIdToNewCatalogId(Integer noteId,
                                            Integer currentCatalogId,
                                            Integer newCatalogId) {

        extractNoteIdByRewritingLinks(noteId, currentCatalogId);

        injectNoteIdAsNextByRewritingLinksAfter(null, noteId, newCatalogId);

        var sql = """
                UPDATE note_catalog_ordering
                SET catalog_id = ?
                WHERE catalog_id = ?
                AND note_id = ?;
                """;

        int updated = jdbcTemplate.update(
                sql,
                newCatalogId,
                currentCatalogId,
                noteId
        );

        if(updated <= 0) {
            throw new NoteNotFoundException(noteId);
        }
    }

    @Override
    public List<NotePreview> selectAllNotesByOwnerIdIncludingCatalogs(Integer ownerId, Integer offset, Integer limit) {
        var sql = """
                WITH RECURSIVE notes_from_catalog_in_user_order AS (
                  SELECT note_id, catalog_id, 0 as level
                  FROM note_catalog_ordering
                  WHERE prev_note_id IS NULL
                  AND catalog_id = (
                    SELECT root_catalog_id
                    FROM users
                    WHERE id = ?
                  )
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
                    c_view.id c_id,
                    c_view.title c_title,
                    u.root_catalog_id u_root_catalog_id
                FROM note n
                JOIN notes_from_catalog_in_user_order nfciuo
                ON n.id = nfciuo.note_id
                LEFT JOIN catalog c
                ON nfciuo.catalog_id = c.id
                JOIN users u
                ON u.id = c.owner_id
                LEFT JOIN note_catalog_ordering nco
                ON nco.note_id = n.id
                AND nco.catalog_id != u.root_catalog_id
                LEFT JOIN catalog c_view
                ON c_view.id = nco.catalog_id
                ORDER BY nfciuo.level
                OFFSET ?
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                notePreviewListMapper,
                ownerId,
                offset,
                limit
        );
    }

    @Override
    public List<NotePreview> selectAllNotesByOwnerIdWithoutCatalogs(Integer ownerId, Integer offset, Integer limit) {
        var sql = """
                WITH RECURSIVE notes_from_catalog_in_user_order AS (
                  SELECT note_id, catalog_id, 0 as level
                  FROM note_catalog_ordering
                  WHERE prev_note_id IS NULL
                  AND catalog_id = (
                    SELECT root_catalog_id
                    FROM users
                    WHERE id = ?
                  )
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
                FROM note n
                JOIN notes_from_catalog_in_user_order nfciuo
                ON n.id = nfciuo.note_id
                LEFT JOIN catalog c
                ON nfciuo.catalog_id = c.id
                JOIN users u
                ON u.id = c.owner_id
                WHERE nfciuo.note_id NOT IN (
                    SELECT DISTINCT note_id
                    FROM note_catalog_ordering nco
                    WHERE nco.catalog_id != u.root_catalog_id
                    AND nco.note_id = n.id
                )
                ORDER BY nfciuo.level
                OFFSET ?
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                notePreviewListMapper,
                ownerId,
                offset,
                limit
        );
    }

    @Override
    public Optional<Integer> selectOwnerIdByNoteId(Integer noteId) {
        var sql = """
                SELECT DISTINCT c.owner_id
                FROM catalog c
                JOIN note_catalog_ordering nco
                ON nco.catalog_id = c.id
                AND nco.note_id = ?;
                """;


        try {
            noteId = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    noteId
            );
        } catch (EmptyResultDataAccessException e) {
            noteId = null;
        }
        return Optional.ofNullable(noteId);
    }


    @Override
    public boolean removeFromNoteCatalogOrdering(Integer noteId, Integer catalogId) {
        var sql = """
                DELETE FROM note_catalog_ordering
                WHERE note_id = ?
                AND catalog_id = ?
                """;

        return jdbcTemplate.update(sql, noteId, catalogId) > 0;
    }

    private String getCatalogSelectingRecursiveSql() {
        return """
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
                FROM note n
                JOIN notes_from_catalog_in_user_order nfciuo
                ON n.id = nfciuo.note_id
                LEFT JOIN catalog c
                ON nfciuo.catalog_id = c.id
                JOIN users u
                ON u.id = c.owner_id
                WHERE %s
                ORDER BY nfciuo.level
                OFFSET ?
                LIMIT ?
                """;
    }

    private void insertIntoNoteCatalogOrderingByRewritingLinks(Integer noteId, Integer catalogId) {
        Integer firstNoteId = selectFirstNoteIdFromCatalog(catalogId);
        if(firstNoteId != null) {
            updatePrevIdAs(noteId, firstNoteId, catalogId);
        }
        insertIntoNoteCatalogOrdering(noteId, catalogId);
    }

    public void insertIntoNoteCatalogOrdering(Integer noteId, Integer catalogId) {
        var sqlOrdering = """
                INSERT INTO note_catalog_ordering(
                    note_id,
                    catalog_id
                ) VALUES (?, ?);
                """;

        jdbcTemplate.update(
                sqlOrdering,
                noteId,
                catalogId
        );
    }

    private void extractNoteIdByRewritingLinks(Integer noteId,
                                               Integer catalogId) {

        Integer nextNoteIdForRewritingNote = findNextIdFor(noteId, catalogId);
        Integer prevNoteIdForRewritingNote = findPrevIdFor(noteId, catalogId);
        updatePrevIdAs(prevNoteIdForRewritingNote, nextNoteIdForRewritingNote, catalogId);

    }
    private void injectNoteIdAsNextByRewritingLinksAfter(Integer prevNoteId,
                                                            Integer noteId,
                                                            Integer catalogId) {

        Integer nextId = prevNoteId == null
                ? findFirstNoteIdFromCatalogId(catalogId)
                : findNextIdFor(prevNoteId, catalogId);
        if(nextId != null) {
            updatePrevIdAs(noteId, nextId, catalogId);
        }
        updatePrevIdAs(prevNoteId, noteId, catalogId);
    }

    private Integer findFirstNoteIdFromCatalogId(Integer catalogId) {
        var sql = """
                SELECT note_id
                FROM note_catalog_ordering
                WHERE catalog_id = ?
                AND prev_note_id IS NULL;
                """;

        Integer nextId;
        try {
            nextId = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    catalogId
            );
        } catch (EmptyResultDataAccessException e) {
            nextId = null;
        }
        return nextId;
    }

    public Integer selectFirstNoteIdFromCatalog(Integer catalogId) {
        var sql = """
                SELECT note_id
                FROM note_catalog_ordering
                WHERE catalog_id = ?
                AND prev_note_id IS NULL
                """;

        Integer firstNoteId;

        try {
            firstNoteId = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    catalogId
            );
        } catch (EmptyResultDataAccessException e) {
            firstNoteId = null;
        }
        return firstNoteId;
    }

    private Integer findNextIdFor(Integer noteId, Integer catalogId) {
        var findingNextNoteIdSql = """
                SELECT note_id
                FROM note_catalog_ordering
                WHERE prev_note_id = ?
                AND catalog_id = ?;
                """;
        Integer nextId;
        try {
            nextId = jdbcTemplate.queryForObject(
                    findingNextNoteIdSql,
                    Integer.class,
                    noteId,
                    catalogId
            );
        } catch (EmptyResultDataAccessException e) {
            nextId = null;
        }
        return nextId;
    }

    private void updatePrevIdAs(Integer prevId,
                                   Integer noteId,
                                   Integer catalogId) {
        var prevIdAsNSql = """
                UPDATE note_catalog_ordering
                SET prev_note_id = ?
                WHERE note_id = ?
                AND catalog_id = ?;
                """;
        jdbcTemplate.update(
                prevIdAsNSql,
                prevId,
                noteId,
                catalogId
        );
    }

    @Override
    public Integer insertEntity(NoteDTO noteDTO) {
        var sql = """
                INSERT INTO note(title, description)
                VALUES(?, ?)
                RETURNING id;
                """;


        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                noteDTO.getName(),
                noteDTO.getDescription()
        );
    }

    @Override
    public boolean removeEntity(Integer entityId, Integer contextId) {
        var sql = """
                DELETE FROM note_catalog_ordering
                WHERE note_id = ?
                AND catalog_id = ?;
                """;

        return jdbcTemplate.update(sql, entityId, contextId) > 0;
    }

    @Override
    public void removeEntity(Integer entityId) {
        var sql = """
                DELETE FROM note
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, entityId);
    }

    @Override
    public List<Integer> findAllContextsForEntity(Integer entityId) {
        var sql = """
                SELECT catalog_id
                FROM note_catalog_ordering
                WHERE note_id = ?
                """;

        return jdbcTemplate.queryForList(sql, Integer.class, entityId);
    }

    @Override
    public void updatePreviousEntityIdByContext(Integer previousEntityId, Integer currentEntityId, Integer contextId) {
        var prevIdAsNSql = """
                UPDATE note_catalog_ordering
                SET prev_note_id = ?
                WHERE note_id = ?
                AND catalog_id = ?;
                """;
        jdbcTemplate.update(
                prevIdAsNSql,
                previousEntityId,
                currentEntityId,
                contextId
        );
    }

    @Override
    public void cleanUp() {
        var sql = """
                DELETE FROM note
                WHERE id NOT IN (
                    SELECT DISTINCT note_id
                    FROM note_catalog_ordering
                )
                """;

        jdbcTemplate.update(sql);
    }

    @Override
    public Integer findFirstEntityIdByContext(Integer contextId) {
        var sql = """
                SELECT note_id
                FROM note_catalog_ordering
                WHERE catalog_id = ?
                AND prev_note_id IS NULL;
                """;

        Integer nextId;
        try {
            nextId = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    contextId
            );
        } catch (EmptyResultDataAccessException e) {
            nextId = null;
        }
        return nextId;
    }

    @Override
    public Integer findNextEntityIdByContext(Integer entityId, Integer contextId) {
        var findingNextNoteIdSql = """
                SELECT note_id
                FROM note_catalog_ordering
                WHERE prev_note_id = ?
                AND catalog_id = ?;
                """;
        Integer nextId;
        try {
            nextId = jdbcTemplate.queryForObject(
                    findingNextNoteIdSql,
                    Integer.class,
                    entityId,
                    contextId
            );
        } catch (EmptyResultDataAccessException e) {
            nextId = null;
        }
        return nextId;
    }

    @Override
    public Integer findPrevEntityIdByContext(Integer entityId, Integer contextId) {
        var sql = """
                SELECT prev_note_id
                FROM note_catalog_ordering nco
                WHERE note_id = ?
                AND catalog_id = ?;
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
