package md.brainet.doeves.note;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class NotePreviewResultSetMapper implements ResultSetExtractor<NotePreview> {

    @Override
    public NotePreview extractData(ResultSet rs) throws SQLException, DataAccessException {
        return null;
    }
}
