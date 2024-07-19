package md.brainet.doeves.catalog;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;


@Service
public class CatalogResultSetMapper implements ResultSetExtractor<Catalog> {

    @Override
    public Catalog extractData(ResultSet rs) throws SQLException, DataAccessException {
        return rs.next() ?  new Catalog(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getInt("prev_catalog_id"),
                rs.getInt("owner_id"),
                rs.getTimestamp("date_of_create").toLocalDateTime()
        ) : null;
    }
}
