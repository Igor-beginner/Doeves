package md.brainet.doeves.note;

import md.brainet.doeves.catalog.Catalog;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoteListResultSetMapper implements ResultSetExtractor<List<Note>> {

    private final NoteResultSetMapper mapper;

    public NoteListResultSetMapper(NoteResultSetMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Note> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Note> notes = new ArrayList<>();

        while (true){
            var catalog = mapper.extractData(rs);
            if(catalog == null) {
                break;
            }
            notes.add(catalog);
        }

        return notes;
    }
}
