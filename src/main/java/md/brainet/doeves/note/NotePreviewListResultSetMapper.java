package md.brainet.doeves.note;

import md.brainet.doeves.catalog.CatalogPreview;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotePreviewListResultSetMapper implements ResultSetExtractor<List<NotePreview>> {

    @Override
    public List<NotePreview> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<NotePreview> notePreviews = new ArrayList<>();
        while (rs.next()) {
            CatalogPreview catalogPreview = null;

            if(rs.getObject("c_id") != null) {
                catalogPreview = new CatalogPreview(
                        rs.getInt("c_id"),
                        rs.getString("c_title")
                );
            }

            notePreviews.add(
                    new NotePreview(
                            rs.getInt("n_id"),
                            rs.getString("n_title"),
                            rs.getString("n_description"),
                            catalogPreview,
                            rs.getTimestamp("n_date_of_create").toLocalDateTime()
                    )
            );
        }

        return notePreviews;
    }
}
