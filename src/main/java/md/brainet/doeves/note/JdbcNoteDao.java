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

    public JdbcNoteDao(JdbcTemplate jdbcTemplate,
                       NoteResultSetMapper noteMapper,
                       NoteListResultSetMapper noteListMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.noteMapper = noteMapper;
        this.noteListMapper = noteListMapper;
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
    public boolean updateOrderNumberByNoteId(Integer noteId, Integer orderNumber) {
        var sqlUpdateOrderNumberForAllAfterUpdated = """
                UPDATE note
                SET order_number = order_number + 1
                WHERE order_number >= ?;
                """;

        var sqlUpdateOrderNumber = """
                UPDATE note
                SET order_number = ?
                WHERE id = ?;
                """;

        jdbcTemplate.update(sqlUpdateOrderNumberForAllAfterUpdated, orderNumber);

        return jdbcTemplate.update(sqlUpdateOrderNumber, orderNumber, noteId) > 0;
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
    public List<Note> selectAllNotesByOwnerIdIncludingCatalogs(Integer ownerId, Integer offset, Integer limit) {
        var sql = """
                SELECT *
                FROM note
                WHERE owner_id = ?
                ORDER BY order_number
                OFFSET ?
                LIMIT ?;
                """;

        return jdbcTemplate.query(sql, noteListMapper, ownerId, offset, limit);
    }

    @Override
    public List<Note> selectAllNotesByOwnerIdWithoutCatalogs(Integer ownerId, Integer offset, Integer limit) {
        var sql = """
                SELECT *
                FROM note
                WHERE owner_id = ?
                AND catalog_id IS NULL
                ORDER BY order_number
                OFFSET ?
                LIMIT ?;
                """;

        return jdbcTemplate.query(sql, noteListMapper, ownerId, offset, limit);
    }
}
