package md.brainet.doeves.task;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TaskListResultSetMapper implements ResultSetExtractor<List<Task>> {

    @Override
    public List<Task> extractData(ResultSet rs) throws SQLException, DataAccessException {

        List<Task> tasks = new ArrayList<>();

        while (rs.next()) {
            Task task = new Task();

            task.setId(rs.getInt("id"));
            task.setName(rs.getString("name"));
            task.setDescription(rs.getString("description"));
            task.setOwnerId(rs.getInt("owner_id"));
            task.setDateOfCreate(
                    rs.getTimestamp("date_of_create")
                            .toLocalDateTime()
            );
            Timestamp deadline = rs.getTimestamp("deadline");
            task.setDeadline(
                    Objects.isNull(deadline)
                            ? null
                            : deadline.toLocalDateTime()
            );
            task.setComplete(rs.getBoolean("is_complete"));

            tasks.add(task);
        }

        return tasks;
    }
}
