package md.brainet.doeves.user;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserResultSetMapper implements ResultSetExtractor<User> {


    @Override
    public User extractData(ResultSet rs) throws SQLException, DataAccessException {

        User user = null;

        if(rs.next()) {
            user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setEnabled(rs.getBoolean("is_enabled"));
            user.setRole(Role.valueOf(rs.getString("role_name")));
            user.setVerified(rs.getBoolean("verified"));
            user.setVerificationDetailsId(rs.getInt("verification_details_id"));
            user.setRootCatalogId(rs.getInt("root_catalog_id"));
        }

        return user;
    }
}
