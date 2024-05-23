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
                r.name as role_name
            FROM users u
            LEFT JOIN users_role ur
            ON ur.user_id = u.id
            LEFT JOIN role r
            ON ur.role_id = r.id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RoleDao roleDao;
    private final UserResultSetMapper resultSetMapper;

    public JDBCUserDao(JdbcTemplate jdbcTemplate,
                       RoleDao roleDao,
                       UserResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.roleDao = roleDao;
        this.resultSetMapper = resultSetMapper;
    }

    @Override
    public Optional<User> selectOwnerOfTaskWithId(int taskId) {
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
    @Transactional
    public Integer insertUserAndDefaultRole(User user) {
        int id;
        try {
            id = insertUser(user);
        } catch (DuplicateKeyException e) {
            throw new EmailAlreadyExistsDaoException(
                    "Email [%s] already exists."
                            .formatted(user.getEmail()),
                    e
            );
        }

        roleDao.insertRoleForUserId(
                id,
                Role.getDefault()
        );

        return id;
    }

    private Integer insertUser(User user) {
        var sql = """
                INSERT INTO users (
                    email,
                    password
                )
                VALUES (?, ?);
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
