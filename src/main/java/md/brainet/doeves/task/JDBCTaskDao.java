package md.brainet.doeves.task;

import md.brainet.doeves.user.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public class JDBCTaskDao implements TaskDao {

    private final JdbcTemplate jdbcTemplate;

    public JDBCTaskDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Task> selectAllTasksWhereUserIdIs(Integer userId) {
        var sql = """
                SELECT *
                FROM task
                WHERE owner_id = ?;
                """;

        return jdbcTemplate.queryForList(sql, Task.class, userId);
    }

    @Override
    public int insertTask(Task task) {
        var sql = """
                INSERT INTO task (
                    name,
                    description,
                    deadline,
                    owner_id
                )
                VALUES (?, ?, ?, ?);
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS
                    );

            preparedStatement.setString(1, task.getName());
            preparedStatement.setString(2, task.getDescription());
            preparedStatement.setTimestamp(3,
                    Timestamp.valueOf(task.getDeadline())
            );
            preparedStatement.setInt(4, task.getOwnerId());

            return preparedStatement;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public void update(Task task) {
        var sql = """
                UPDATE task
                SET name = ?,
                description = ?,
                deadline = ?
                WHERE id = ?;
                """;

        jdbcTemplate.update(sql,
                task.getName(),
                task.getDescription(),
                task.getDeadline(),
                task.getId()
        );
    }

    @Override
    public void removeById(int taskId) {
        var sql = """
                DELETE FROM task
                WHERE id = ?;
                """;

        jdbcTemplate.update(sql, taskId);
    }

    @Override
    public void changeTaskStatusById(int taskId, boolean complete) {
        var sql = """
                UPDATE task
                SET is_complete = ?
                WHERE id = ?;
                """;

        jdbcTemplate.update(sql, complete, taskId);
    }

    @Override
    public void updateStatusByTaskId(int taskId, boolean complete) {
        var sql = """
                UPDATE task
                SET is_complete = ?
                WHERE id = ?;
                """;

        jdbcTemplate.update(sql, complete, taskId);
    }
}
