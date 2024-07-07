package md.brainet.doeves.note;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

public class JdbcNoteDao implements NoteDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNoteDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Note> insertNote(Integer ownerId, NoteDTO noteDTO) {
        return Optional.empty();
    }

    @Override
    public boolean updateOrderNumberByNoteId(Integer noteId, Integer orderNumber) {
        return false;
    }

    @Override
    public boolean updateNameByNoteId(Integer noteId, String noteName) {
        return false;
    }

    @Override
    public boolean updateDescriptionByNoteId(Integer noteId, String noteDescription) {
        return false;
    }

    @Override
    public boolean removeByNoteId(Integer noteId) {
        return false;
    }

    @Override
    public Optional<Note> selectByNoteId(Integer noteId) {
        return Optional.empty();
    }

    @Override
    public boolean updateCatalogId(Integer catalogId, Integer forNoteId) {
        return false;
    }

    @Override
    public List<Note> selectAllNotesByOwnerIdIncludingCatalogs(Integer ownerId, Integer offset, Integer limit) {
        return null;
    }

    @Override
    public List<Note> selectAllNotesByOwnerIdWithoutCatalogs(Integer ownerId, Integer offset, Integer limit) {
        return null;
    }
}
