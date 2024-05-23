package md.brainet.doeves.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDaoImpl implements RoleDao {

    private final JdbcTemplate jdbcTemplate;

    public RoleDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertRoleForUserId(Integer userId, Role role) {
        var sql = """
                INSERT INTO users_role (user_id, role_id)
                SELECT ?, id
                FROM role
                WHERE name = ?;
                """;

        jdbcTemplate.update(sql, userId, role.name());
    }
}
