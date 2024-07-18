package md.brainet.doeves.note;

import md.brainet.doeves.user.User;
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
                INSERT INTO note(title, description, catalog_id)
                VALUES(?, ?, ?)
                RETURNING *;
                """;

        var note = Optional.ofNullable(
                jdbcTemplate.query(
                        sql,
                        noteMapper,
                        noteDTO.getName(),
                        noteDTO.getDescription(),
                        noteDTO.getCatalogId(),
                        user.getId()
                )
        );

        if(note.isEmpty()) {
            return note;
        }

        Note concreteNote = note.get();

        int noteId = concreteNote.id();

        insertIntoNoteCatalogOrderingByRewritingLinks(noteId, noteDTO.getCatalogId());
        insertIntoNoteCatalogOrderingByRewritingLinks(noteId, user.getRootCatalogId());
        return note;
    }

    @Override
    @Transactional
    public boolean updateOrderNumberByNoteId(Integer prevNoteId, Integer noteId, Integer catalogId) {
        extractNoteIdByRewritingLinks(prevNoteId, noteId, catalogId);
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

        Integer prevNoteId = findPrevIdFor(noteId);

        int nextNoteIdOfDeletingNote = findNextIdFor(noteId, catalogId);

        updatePrevIdAs(prevNoteId, nextNoteIdOfDeletingNote, catalogId);

        return jdbcTemplate.update(sql, noteId, catalogId) > 0;
    }

    private Integer findPrevIdFor(Integer noteId) {
        var sql = """
                SELECT prev_id
                FROM note_catalog_ordering nco
                WHERE note_id = ?;
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                noteId
        );
    }

    @Override
    public Optional<Note> selectByNoteId(Integer noteId) {
        var sql = """
                SELECT *
                FROM note
                WHERE id = ?;
                """;

        return Optional.ofNullable(
                jdbcTemplate.query(
                        sql, noteMapper, noteId
                )
        );
    }

    @Override
    @Transactional
    public boolean moveNoteIdToNewCatalogId(Integer noteId,
                                            Integer currentCatalogId,
                                            Integer newCatalogId) {

        Integer firstNoteIdFromNewCatalog = selectFirstNoteIdFromCatalog(newCatalogId);

        if(firstNoteIdFromNewCatalog == null) {
            return false;
        }

        updatePrevIdAs(noteId, firstNoteIdFromNewCatalog, newCatalogId);

        updatePrevIdAs(null, noteId, currentCatalogId);

        return true;
    }

    @Override
    public List<NotePreview> selectAllNotesByOwnerIdIncludingCatalogs(Integer ownerId, Integer offset, Integer limit) {
        var sql = getCatalogSelectingRecursiveSql()
                .formatted("""
                        WHERE nfciuo.catalog_id IN((
                            SELECT id
                            FROM catalog
                            WHERE owner_id = ?
                        ))
                        """);

        return jdbcTemplate.query(
                sql,
                notePreviewListMapper,
                ownerId,
                offset,
                limit
        );
    }

    @Override
    public List<NotePreview> selectAllNotesByOwnerIdWithoutCatalogs(Integer rootCatalogId, Integer offset, Integer limit) {
        var sql = getCatalogSelectingRecursiveSql()
                .formatted("WHERE nfciuo.catalog_id = ?");

        return jdbcTemplate.query(
                sql,
                notePreviewListMapper,
                rootCatalogId,
                offset,
                limit
        );
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
                    DISTINCT n.id n_id,
                    n.title n_title,
                    n.description n_description,
                    n.date_of_create n_date_of_create,
                    c.id c_id,
                    c.title c_title,
                    n.catalog_id n_catalog_id,
                    no.order_number no_order_number
                FROM note n
                LEFT JOIN catalog c
                ON n.catalog_id = c.id
                JOIN notes_from_catalog_in_user_order nfciuo
                ON n.id = nfciuo.note_id
                WHERE %s
                ORDER BY nfciuo.level
                OFFSET ?
                LIMIT ?
                """;
    }

    private void insertIntoNoteCatalogOrderingByRewritingLinks(Integer noteId, Integer catalogId) {
        Integer firstNoteId = selectFirstNoteIdFromCatalog(catalogId);
        updatePrevIdAs(noteId, firstNoteId, catalogId);
        insertIntoNoteCatalogOrdering(noteId, catalogId);
    }

    private void insertIntoNoteCatalogOrdering(Integer noteId, Integer catalogId) {
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

    private void extractNoteIdByRewritingLinks(Integer prevNoteId,
                                                  Integer noteId,
                                                  Integer catalogId) {

        Integer nextNoteIdForRewritingNote = findNextIdFor(noteId, catalogId);

        updatePrevIdAs(prevNoteId, nextNoteIdForRewritingNote, catalogId);
    }

    private void injectNoteIdAsNextByRewritingLinksAfter(Integer prevNoteId,
                                                            Integer noteId,
                                                            Integer catalogId) {

        Integer nextId = findNextIdFor(noteId, catalogId);
        updatePrevIdAs(noteId, nextId, catalogId);
        updatePrevIdAs(prevNoteId, noteId, catalogId);
    }

    private Integer findNextIdFor(Integer noteId, Integer catalogId) {
        var findingNextNoteIdSql = """
                SELECT note_id
                FROM note_catalog_ordering
                WHERE prev_id = ?
                AND catalog_id = ?;
                """;

        return jdbcTemplate.queryForObject(
                findingNextNoteIdSql,
                Integer.class,
                noteId,
                catalogId
        );
    }

    private void updatePrevIdAs(Integer prevId,
                                   Integer noteId,
                                   Integer catalogId) {
        var prevIdAsNSql = """
                UPDATE note_catalog_ordering
                SET prev_id = ?
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

    private Integer selectFirstNoteIdFromCatalog(Integer catalogId) {
        var sql = """
                SELECT note_id
                FROM note_catalog_ordering
                WHERE catalog_id = ?
                AND prev_note IS NULL
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                catalogId
        );
    }
}
