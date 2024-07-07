package md.brainet.doeves.catalog;

import md.brainet.doeves.note.Note;
import md.brainet.doeves.note.NoteListResultSetMapper;
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
    private final NoteListResultSetMapper noteListMapper;

    public JdbcCatalogDao(JdbcTemplate jdbcTemplate,
                          CatalogResultSetMapper catalogMapper,
                          CatalogListResultSetMapper catalogListMapper,
                          NoteListResultSetMapper noteListMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalogMapper = catalogMapper;
        this.catalogListMapper = catalogListMapper;
        this.noteListMapper = noteListMapper;
    }

    @Override
    public Catalog insertCatalog(CatalogDTO catalogDTO) {
        var sql = """
                INSERT INTO catalog(title, owner_id, order_number)
                VALUES(?, ?, ?)
                RETURNING id, title, owner_id, order_number, date_of_create
                """;
        return jdbcTemplate.query(sql, catalogMapper,
                catalogDTO.name(),
                catalogDTO.ownerId(),
                catalogDTO.orderNumber()
        );
    }

    @Override
    public Optional<Catalog> selectCatalogById(Integer id) {
        var sql = """
                SELECT
                   id,
                   title,
                   owner_id,
                   order_number,
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
                SELECT
                   c.id,
                   c.title,
                   c.owner_id,
                   c.order_number,
                   c.date_of_create
                FROM catalog c
                INNER JOIN users u
                ON u.id = c.owner_id
                WHERE c.owner_id = ?
                OFFSET ?
                LIMIT ?;
                """;

        return jdbcTemplate.query(sql, catalogListMapper, ownerId, offset, limit);
    }

    @Override
    @Transactional
    public boolean updateOrderNumberByCatalogId(Integer catalogId, Integer orderNumber) {
        var sqlUpdateOrderNumberForAllAfterUpdated = """
                UPDATE catalog
                SET order_number = order_number + 1
                WHERE order_number >= ?;
                """;

        var sqlUpdateOrderNumber = """
                UPDATE catalog 
                SET order_number = ?
                WHERE id = ?;
                """;

        jdbcTemplate.update(sqlUpdateOrderNumberForAllAfterUpdated, orderNumber);

        return jdbcTemplate.update(sqlUpdateOrderNumber, orderNumber, catalogId) > 0;
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
    public boolean removeByCatalogId(Integer catalogId) {
        var sql = """
                DELETE FROM catalog
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql, catalogId) > 0;
    }

    @Override
    public List<Note> selectAllNotesByCatalogId(Integer catalogId, Integer offset, Integer limit) {
        var sql = """
                SELECT *
                FROM note
                WHERE catalog_id = ?
                OFFSET ?
                LIMIT ?;
                """;

        return jdbcTemplate.query(sql, noteListMapper, catalogId, offset, limit);
    }
}
