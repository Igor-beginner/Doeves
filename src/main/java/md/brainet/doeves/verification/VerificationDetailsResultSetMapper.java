package md.brainet.doeves.verification;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class VerificationDetailsResultSetMapper
        implements ResultSetExtractor<VerificationDetails> {

    @Override
    public VerificationDetails extractData(ResultSet rs) throws SQLException, DataAccessException {
        VerificationDetails verificationDetails = null;

        if(rs.next()) {
            verificationDetails = new VerificationDetails();
            verificationDetails.setId(rs.getInt("id"));
            verificationDetails.setCode(rs.getString("code"));
            verificationDetails.setExpireDate(
                    rs.getTimestamp("expire_date")
                            .toLocalDateTime()
            );
            verificationDetails.setMissingAttempts(
                    rs.getInt("missing_attempts")
            );
        }

        return verificationDetails;
    }
}
