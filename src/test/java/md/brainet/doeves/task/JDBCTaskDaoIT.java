package md.brainet.doeves.task;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.TestConfig;
import md.brainet.doeves.user.JDBCUserDao;
import md.brainet.doeves.user.User;
import md.brainet.doeves.user.UserResultSetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestConfig.class})
class JDBCTaskDaoIT extends IntegrationTestBase {

    @Autowired
    JdbcTemplate jdbcTemplate;

    TaskListResultSetMapper taskListResultSetMapper;

    JDBCTaskDao jdbcTaskDao;

    @BeforeEach
    void setUp() {
        taskListResultSetMapper = new TaskListResultSetMapper();
        jdbcTaskDao = new JDBCTaskDao(
                jdbcTemplate,
                taskListResultSetMapper
        );
    }

    @Test
    void selectAllTasksWhereUserIdIs1_Return3Tasks() {
        //given
        var userId = 1;
        var expectedListLength = 3;

        //when
        List<Task> response = jdbcTaskDao.selectAllTasksWhereUserIdIs(userId);

        //then
        assertEquals(expectedListLength, response.size());
    }

    @Test
    void insertTask_expectCorrectGeneratedId() {
        //given
        var expectedTaskId = 4;

        var task = new Task();
        task.setName("teeeest");
        task.setOwnerId(1);

        //when
        var actualTaskId = jdbcTaskDao.insertTask(task);

        //then
        assertEquals(expectedTaskId, actualTaskId);
    }

    @Test
    void update_taskExists_checkChanges() {
        //given
        var newName = "23123";
        var newDescription = "31231";

        Task task = jdbcTaskDao.selectById(1).get();
        task.setName(newName);
        task.setDescription(newDescription);

        //when
        jdbcTaskDao.update(task);

        //then
        task = jdbcTaskDao.selectById(1).get();
        assertEquals(newName, task.getName());
        assertEquals(newDescription, task.getDescription());
    }

    @Test
    void update_taskNotExists_checkChanges() {
        //given
        var newId = 1010;
        var newName = "23123";
        var newDescription = "31231";

        Task task = jdbcTaskDao.selectById(1).get();
        task.setId(newId);
        task.setName(newName);
        task.setDescription(newDescription);

        //when
        boolean updated = jdbcTaskDao.update(task);

        //then
        assertFalse(updated);
        var foundTask = jdbcTaskDao.selectById(newId);
        assertFalse(foundTask.isPresent());
    }

    @Test
    void removeById_suchTaskExists_expectTrue() {
        //given
        var taskId = 1;

        //when
        boolean removed = jdbcTaskDao.removeById(taskId);

        //then
        assertTrue(removed);
    }

    @Test
    void removeById_suchTaskNotExists_expectFalse() {
        //given
        var taskId = 32;

        //when
        boolean removed = jdbcTaskDao.removeById(taskId);

        //then
        assertFalse(removed);
    }

    @Test
    void updateStatusByTaskId_suchTaskExists_expectMadeChanges() {
        //given
        var task = jdbcTaskDao.selectById(1);

        //when
        boolean updated = jdbcTaskDao
                .updateStatusByTaskId(task.get().getId(), true);

        //then
        task = jdbcTaskDao.selectById(1);
        assertTrue(task.get().isComplete());
        assertTrue(updated);
    }

    @Test
    void updateStatusByTaskId_suchTaskNotExists_expectZero() {
        //given
        var taskId = 32;

        //when
        boolean updated = jdbcTaskDao
                .updateStatusByTaskId(taskId, true);

        //then
        assertFalse(updated);
    }

    @Test
    void userExists_whenUserExists_expectTrue() {
        //given
        var userId = 1;

        //when
        boolean exists = jdbcTaskDao.userExists(userId);

        //then
        assertTrue(exists);
    }

    @Test
    void userExists_whenUserNotExists_expectFalse() {
        //given
        var userId = 32;

        //when
        boolean exists = jdbcTaskDao.userExists(userId);

        //then
        assertFalse(exists);
    }
}