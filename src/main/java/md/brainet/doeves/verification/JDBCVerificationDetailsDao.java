package md.brainet.doeves.verification;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class JDBCVerificationDetailsDao implements VerificationDetailsDao {

    private final JdbcTemplate jdbcTemplate;
    private final VerificationDetailsResultSetMapper mapper;


    public JDBCVerificationDetailsDao(
            JdbcTemplate jdbcTemplate,
            VerificationDetailsResultSetMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public Optional<VerificationDetails> selectVerificationDetailsByEmail(String email) {
        var sql = """
                 UPDATE verification_details
                 SET missing_attempts = missing_attempts - 1
                 WHERE id = (
                     SELECT verification_details_id
                     FROM users 
                     WHERE email = ?
                 )
                 RETURNING *;
                 """;

        return Optional.ofNullable(
                jdbcTemplate.query(sql, mapper, email)
        );
    }

    @Override
    public boolean updateVerificationDetails(String email, VerificationDetails verificationDetails) {
        var sql = """
                UPDATE verification_details
                SET code = ?, expire_date = ?, missing_attempts = ?
                WHERE id = (
                    SELECT verification_details_id
                    FROM users
                    WHERE email = ?
                );
                """;
        return jdbcTemplate.update(
                sql,
                verificationDetails.getCode(),
                verificationDetails.getExpireDate(),
                verificationDetails.getMissingAttempts(),
                email

        ) > 0;
    }

    @Override
    public Integer insertVerificationDetails(VerificationDetails details) {
        var sql = """
                INSERT INTO verification_details (
                    code,
                    expire_date
                ) VALUES (?, ?);
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS
                    );

            preparedStatement.setString(1, details.getCode());
            preparedStatement.setTimestamp(
                    2,
                    Timestamp.valueOf(details.getExpireDate())
            );

            return preparedStatement;
        }, keyHolder);

        return  (int) keyHolder.getKeys().get("id");
    }

    @Override
    public boolean verifyUserByEmail(String email) {
        var sql = """
                UPDATE users
                SET verified = true
                WHERE email = ?;
                """;

        return jdbcTemplate.update(sql, email) > 0;
    }

    @Override
    public boolean updateUserVerificationDetailsId(String email,
                                                   Integer newVerificationDetailsId) {
        var sql = """
                UPDATE users
                SET verification_details_id = ?
                WHERE email = ?
                """;

        return jdbcTemplate.update(sql, newVerificationDetailsId, email) > 0;
    }

    @Override
    public boolean isUserVerified(String email) {
        var sql = """
                SELECT verified
                FROM users
                WHERE email = ?;
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, email));
    }
}
