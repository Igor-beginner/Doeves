package md.brainet.doeves.note;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EmptyStackException;
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
    public Optional<Note> insertNote(Integer ownerId, NoteDTO noteDTO) {

        var sql = """
                INSERT INTO note(title, description, catalog_id, owner_id)
                VALUES(?, ?, ?, ?)
                RETURNING *;
                """;

        var note = Optional.ofNullable(
                jdbcTemplate.query(
                        sql,
                        noteMapper,
                        noteDTO.name(),
                        noteDTO.description(),
                        noteDTO.catalogId(),
                        ownerId
                )
        );

        if(note.isEmpty()) {
            return note;
        }

        Note concreteNote = note.get();

        int noteId = concreteNote.id();

        var sqlShifting = """
                UPDATE note_order
                SET order_number = order_number + 1
                WHERE context_id = ?
                """;

        var sqlOrdering = """
                INSERT INTO note_order(
                    note_id,
                    context,
                    order_number
                ) VALUES (?, ?, ?);
                """;

        if(noteDTO.catalogId() != null) {
            jdbcTemplate.update(
                    sqlShifting,
                    ViewContext.CATALOG.getContextId(concreteNote)
            );

            jdbcTemplate.update(
                    sqlOrdering,
                    noteId,
                    ViewContext.CATALOG,
                    0
            );
        }

        jdbcTemplate.update(
                sqlShifting,
                ViewContext.HOME_PAGE.getContextId(concreteNote)
        );

        jdbcTemplate.update(
                sqlOrdering,
                noteId,
                ViewContext.HOME_PAGE,
                0
        );
        return note;
    }

    @Override
    @Transactional
    public boolean updateOrderNumberByNoteId(NoteOrderingRequest request) {
        var shiftInFront = request.newOrderNumber() > request.currentOrderNumber();
        var conditionalToShift = shiftInFront
                ? ">= ?"
                : "BETWEEN ? + 1 AND ? - 1";

        var sqlShifting = """
                UPDATE note_order
                SET order_number = order_number + 1
                WHERE order_number %s
                AND note_id = ?
                AND context = ?
                AND context_id = ?
                """.formatted(conditionalToShift);

        boolean shiftUpdated;

        if(shiftInFront) {
            shiftUpdated = jdbcTemplate.update(
                    sqlShifting,
                    request.newOrderNumber(),
                    request.noteId(),
                    request.context(),
                    request.contextId()
            ) > 0;
        } else {
            shiftUpdated = jdbcTemplate.update(
                    sqlShifting,
                    request.newOrderNumber(),
                    request.currentOrderNumber(),
                    request.noteId(),
                    request.context(),
                    request.contextId()
            ) > 0;
        }

        var sqlUpdating = """
                UPDATE note_order
                SET order_number = ?
                WHERE note_id = ?
                AND context = ?
                """;

        return jdbcTemplate.update(
                sqlUpdating,
                request.newOrderNumber(),
                request.noteId(),
                request.context()
        ) > 0 && shiftUpdated;
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
    public boolean removeByNoteId(Integer noteId) {
        var sql = """
                DELETE FROM note
                WHERE id = ?
                """;

        return jdbcTemplate.update(sql, noteId) > 0;
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
    public boolean updateCatalogIdForNote(Integer catalogId, Integer forNoteId) {
        var sql = """
                UPDATE note
                SET catalog_id = ?
                WHERE id = ?;
                """;

        return jdbcTemplate.update(sql, catalogId, forNoteId) > 0;
    }

    @Override
    public List<NotePreview> selectAllNotesByOwnerIdIncludingCatalogs(Integer ownerId, Integer offset, Integer limit) {
        var sql = """
                SELECT 
                    n.id n_id,
                    n.title n_title,
                    n.description n_description,
                    n.date_of_create n_date_of_create,
                    c.id c_id,
                    c.title c_title,
                    n.catalog_id n_catalog_id,
                    no.order_number no_order_number
                FROM note n
                INNER JOIN catalog c
                ON n.catalog_id = c.id
                INNER JOIN note_oreder no
                ON no.note_id = n.id
                WHERE n.owner_id = ?
                AND no.context = ?
                ORDER BY no.order_number
                OFFSET ?
                LIMIT ?;
                """;
        //todo insert context_id
        return jdbcTemplate.query(
                sql,
                notePreviewListMapper,
                ownerId,
                ViewContext.HOME_PAGE,
                offset,
                limit
        );
    }

    @Override
    public List<NotePreview> selectAllNotesByOwnerIdWithoutCatalogs(Integer ownerId, Integer offset, Integer limit) {
        var sql = """
                SELECT 
                    n.id n_id,
                    n.title n_title,
                    n.description n_description,
                    n.date_of_create n_date_of_create,
                    n.catalog_id n_catalog_id,
                    no.order_number no_order_number
                FROM note n
                INNER JOIN note_oreder no
                ON no.note_id = n.id
                WHERE n.owner_id = ?
                AND no.context = ?
                AND n.catalog_id IS NULL
                ORDER BY no.order_number
                OFFSET ?
                LIMIT ?;
                """;

        return jdbcTemplate.query(
                sql,
                notePreviewListMapper,
                ownerId,
                ViewContext.HOME_PAGE,
                offset,
                limit
        );
    }
}
