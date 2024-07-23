package md.brainet.doeves.note;

import md.brainet.doeves.catalog.CatalogPreview;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class NotePreviewListResultSetMapper implements ResultSetExtractor<List<NotePreview>> {

    @Override
    public List<NotePreview> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Set<NotePreview> notePreviews = new LinkedHashSet<>();
        while (rs.next()) {
            CatalogPreview catalogPreview = null;
            Integer catalogId = (Integer) rs.getObject("c_id");
            if(catalogId != null
                && rs.getObject("u_root_catalog_id") != catalogId) {
                catalogPreview = new CatalogPreview(
                        catalogId,
                        rs.getString("c_title")
                );
            }
            var notePreview = new NotePreview(
                    rs.getInt("n_id"),
                    rs.getString("n_title"),
                    rs.getString("n_description"),
                    catalogPreview,
                    rs.getTimestamp("n_date_of_create").toLocalDateTime()
            );
            notePreviews.add(notePreview);
        }

        return notePreviews.stream().toList();
    }
}
