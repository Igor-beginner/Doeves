package md.brainet.doeves.note;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class NoteResultSetMapper implements ResultSetExtractor<Note> {

    @Override
    public Note extractData(ResultSet rs) throws SQLException, DataAccessException {
        return rs.next() ? new Note(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getTimestamp("date_of_create").toLocalDateTime(),
                rs.getInt("catalog_id"),
                rs.getInt("order_number"),
                rs.getInt("owner_id")
        ) : null;
    }
}