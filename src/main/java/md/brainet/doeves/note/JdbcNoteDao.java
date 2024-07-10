package md.brainet.doeves.note;

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
    public Optional<Note> insertNote(Integer ownerId, NoteDTO noteDTO) {
        var sql = """
                INSERT INTO note(title, description, catalog_id, order_number, owner_id)
                VALUES(?, ?, ?, ?, ?)
                RETURNING *;
                """;

        return Optional.ofNullable(
                jdbcTemplate.query(
                        sql,
                        noteMapper,
                        noteDTO.name(),
                        noteDTO.description(),
                        noteDTO.catalogId(),
                        noteDTO.orderNumber(),
                        ownerId
                )
        );
    }

    @Override
    @Transactional
    public boolean updateOrderNumberByNoteId(NoteOrderingRequest request) {
        //todo need to add conditional with catalog
        var sqlUpdateOrderNumberForAllAfterUpdated = """
                UPDATE note
                SET order_number = order_number + 1
                WHERE order_number >= ?
                AND catalog_id = ?;
                """;

        var sqlUpdateOrderNumber = """
                UPDATE note
                SET order_number = ?
                WHERE id = ?;
                """;

        jdbcTemplate.update(sqlUpdateOrderNumberForAllAfterUpdated, request.orderNumber(), request.catalogId());

        return jdbcTemplate.update(sqlUpdateOrderNumber, request.orderNumber(), request.noteId()) > 0;
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
