package md.brainet.doeves.catalog;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogListResultSetMapper implements ResultSetExtractor<List<Catalog>> {

    private final CatalogResultSetMapper catalogResultSetMapper;

    public CatalogListResultSetMapper(CatalogResultSetMapper catalogResultSetMapper) {
        this.catalogResultSetMapper = catalogResultSetMapper;
    }

    @Override
    public List<Catalog> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Catalog> catalogs = new ArrayList<>();

        while (true){
            var catalog = catalogResultSetMapper.extractData(rs);
            if(catalog == null) {
                break;
            }
            catalogs.add(catalog);
        }

        return catalogs;
    }
}
