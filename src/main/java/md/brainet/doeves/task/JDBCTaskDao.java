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
import java.util.NoSuchElementException;
import java.util.Objects;

@Repository
public class JDBCTaskDao implements TaskDao {

    private final JdbcTemplate jdbcTemplate;
    private final TaskListResultSetMapper taskListResultSetMapper;

    public JDBCTaskDao(JdbcTemplate jdbcTemplate, TaskListResultSetMapper taskListResultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.taskListResultSetMapper = taskListResultSetMapper;
    }

    @Override
    public List<Task> selectAllTasksWhereUserIdIs(Integer userId) {
        var sql = "SELECT * FROM task WHERE owner_id = ?;";

        return jdbcTemplate.query(
                sql,
                taskListResultSetMapper,
                userId
        );
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
            var deadline = task.getDeadline();
            preparedStatement.setTimestamp(3,
                    Objects.isNull(deadline)
                    ? null
                    : Timestamp.valueOf(deadline)
            );
            preparedStatement.setInt(4, task.getOwnerId());

            return preparedStatement;
        }, keyHolder);

        return (int) keyHolder.getKeys().get("id");
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
        //TODO throw exception where task id doesn't exist
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
    public void updateStatusByTaskId(int taskId, boolean complete) {
        var sql = """
                UPDATE task
                SET is_complete = ?
                WHERE id = ?;
                """;

        jdbcTemplate.update(sql, complete, taskId);
    }

    @Override
    public Task selectById(int taskId) {
        var sql = """
                SELECT *
                FROM task
                WHERE id = ?;
                """;

        List<Task> tasks = jdbcTemplate
                .query(sql, taskListResultSetMapper, taskId);

        if(Objects.isNull(tasks) || tasks.isEmpty()) {
            throw new NoSuchElementException(
                    "Task with id [%s] cannot be found"
                            .formatted(tasks)
            );
        }
        return tasks.get(0);
    }
}
