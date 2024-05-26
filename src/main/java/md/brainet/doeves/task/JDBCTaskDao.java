package md.brainet.doeves.task;

import md.brainet.doeves.exception.UserNotFoundException;
import md.brainet.doeves.user.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class JDBCTaskDao implements TaskDao {

    private final JdbcTemplate jdbcTemplate;
    private final TaskListResultSetMapper taskListResultSetMapper;

    public JDBCTaskDao(JdbcTemplate jdbcTemplate, TaskListResultSetMapper taskListResultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.taskListResultSetMapper = taskListResultSetMapper;
    }

    @Override
    @Transactional
    public List<Task> selectAllTasksWhereUserIdIs(Integer userId) {
        var sql = "SELECT * FROM task WHERE owner_id = ?;";

        var tasks =  jdbcTemplate.query(
                sql,
                taskListResultSetMapper,
                userId
        );

        if((tasks == null || tasks.isEmpty()) && !userExists(userId)) {
            throw new UserNotFoundException(userId);
        }

        return tasks;
    }

    @Override
    public int insertTask(Task task) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            var sql = """
                
                    INSERT INTO task (
                    name,
                    description,
                    deadline,
                    owner_id
                )
                VALUES (?, ?, ?, ?);
               """;

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
        } catch (DataIntegrityViolationException e) {
            throw new UserNotFoundException(task.getId(), e);
        }

        return (int) keyHolder.getKeys().get("id");
    }

    @Override
    public boolean update(Task task) {
        var sql = """
                UPDATE task
                SET name = ?,
                description = ?,
                deadline = ?
                WHERE id = ?;
                """;
        int updatedAmount = jdbcTemplate.update(sql,
                task.getName(),
                task.getDescription(),
                task.getDeadline(),
                task.getId()
        );
        return updatedAmount > 0;
    }

    @Override
    public boolean removeById(int taskId) {
        var sql = """
                DELETE FROM task
                WHERE id = ?;
                """;

        return jdbcTemplate.update(sql, taskId) > 0;
    }

    @Override
    public boolean updateStatusByTaskId(int taskId, boolean complete) {
        var sql = """
                UPDATE task
                SET is_complete = ?
                WHERE id = ?;
                """;

        return jdbcTemplate.update(sql, complete, taskId) > 0;
    }

    @Override
    public boolean userExists(int userId) {
        var sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate
                .queryForObject(sql, Integer.class, userId);

        return count != null && count > 0;
    }

    @Override
    public Optional<Task> selectById(int taskId) {
        var sql = """
                SELECT *
                FROM task
                WHERE id = ?;
                """;

        List<Task> tasks = jdbcTemplate
                .query(sql, taskListResultSetMapper, taskId);

        if(Objects.isNull(tasks) || tasks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(tasks.get(0));
    }
}
