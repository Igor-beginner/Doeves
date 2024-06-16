package md.brainet.doeves.user;

import md.brainet.doeves.exception.EmailAlreadyExistsDaoException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class JDBCUserDao implements UserDao {

    private final static String SELECT_USER_BY_ID_SQL = """
            SELECT
                u.id,
                u.email,
                u.is_enabled,
                u.password,
                u.verified,
                u.verification_details_id,
                r.name as role_name
            FROM users u
            LEFT JOIN role r
            ON u.role_id = r.id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final UserResultSetMapper resultSetMapper;

    public JDBCUserDao(JdbcTemplate jdbcTemplate,
                       UserResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    @Override
    public Optional<User> selectOwnerOfTaskWithId(Integer taskId) {
        var sql = SELECT_USER_BY_ID_SQL + """
                LEFT JOIN task t
                ON t.owner_id = u.id
                WHERE t.id = ?
                """;

        return Optional.ofNullable(
                jdbcTemplate.query(
                        sql,
                        resultSetMapper,
                        taskId
                )
        );
    }

    @Override
    public Integer insertUserAndDefaultRole(User user) {
        var sql = """
                INSERT INTO users (
                    email,
                    password,
                    verification_details_id
                )
                VALUES (?, ?, ?);
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS
                    );

            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setObject(3, user.getVerificationDetailsId());

            return preparedStatement;
        }, keyHolder);

        return  (int) keyHolder.getKeys().get("id");
    }


    @Override
    public Optional<User> selectUserById(Integer userId) {
        var sql = SELECT_USER_BY_ID_SQL + """
                WHERE u.id = ?;
                """;

        return selectUserByCriteria(userId, sql);
    }

    @Override
    public Optional<User> selectUserByEmail(String email) {
        var sql = SELECT_USER_BY_ID_SQL + """
                WHERE email = ?;
                """;

        return selectUserByCriteria(email, sql);
    }

    @Override
    public boolean changeUserRoleByUserId(Integer userId, Role role) {
        var sql = """
                UPDATE users
                SET role_id = (
                    SELECT id
                    FROM role
                    WHERE name = ?
                )
                WHERE id = ?;
                """;

        return jdbcTemplate.update(sql, role.name(), userId) > 0;
    }

    private Optional<User> selectUserByCriteria(
            Object criteria,
            String sql) {


        try {
            return Optional.ofNullable(
                    jdbcTemplate.query(
                            sql,
                            resultSetMapper,
                            criteria
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
