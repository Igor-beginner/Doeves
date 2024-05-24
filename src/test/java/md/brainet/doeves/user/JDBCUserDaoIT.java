package md.brainet.doeves.user;

import md.brainet.doeves.IntegrationTestBase;
import md.brainet.doeves.TestConfig;
import md.brainet.doeves.exception.EmailAlreadyExistsDaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestConfig.class})
class JDBCUserDaoIT extends IntegrationTestBase {

    @Autowired
    JdbcTemplate jdbcTemplate;

    UserResultSetMapper userResultSetMapper;

    JDBCUserDao jdbcUserDao;

    @BeforeEach
    void setUp() {
        userResultSetMapper = new UserResultSetMapper();
        jdbcUserDao = new JDBCUserDao(
                jdbcTemplate,
                userResultSetMapper
        );
    }

    @Test
    @Sql(scripts = "classpath:data/init_test_data.sql")
    void selectOwnerOfTaskWithId_taskIdExists_expectCorrectOwner() {
        //given
        var taskId = 1;

        User user = new User();
        user.setId(1);
        user.setEmail("test@mail.ru");
        user.setPassword("123456");
        user.setRole(Role.USER);

        //when
        Optional<User> owner =
                jdbcUserDao.selectOwnerOfTaskWithId(taskId);

        //then
        assertTrue(owner.isPresent());
        assertEquals(user.getEmail(), owner.get().getEmail());
        assertEquals(user.getId(), owner.get().getId());
        assertEquals(user.getRole(), owner.get().getRole());
    }

    @Test
    @Sql(scripts = "classpath:data/init_test_data.sql")
    void selectOwnerOfTaskWithId_taskIdNotExists_expectException() {
        //given
        var taskId = 200;

        //when
        Optional<User> user = jdbcUserDao.selectOwnerOfTaskWithId(taskId);

        //then
        assertFalse(user.isPresent());
    }

    @Test
    @Sql(scripts = "classpath:data/init_test_data.sql")
    void insertUser_emailUniqueAndPasswordValid_expectCorrectId() {
        //given
        var expectedId = 3;
        var user = new User();
        user.setEmail("valera777@gmail.com");
        user.setPassword("23123123");

        //when
        Integer actualId = jdbcUserDao.insertUserAndDefaultRole(user);

        //then
        assertEquals(expectedId, actualId);
    }

    @Test
    @Sql(scripts = "classpath:data/init_test_data.sql")
    void insertUser_emailAlreadyExists_expectException() {
        //given
        var user = new User();
        user.setEmail("test@mail.ru");
        user.setPassword("23123123");

        //when
        Executable performing = () -> jdbcUserDao.insertUserAndDefaultRole(user);

        //then
        assertThrows(DuplicateKeyException.class, performing);
    }

    @Test
    @Sql(scripts = "classpath:data/init_test_data.sql")
    void selectUserById_idExists_expectCorrectEmail() {
        //given
        var id = 1;
        var email = "test@mail.ru";

        //when
        Optional<User> user = jdbcUserDao.selectUserById(id);

        //then
        assertTrue(user.isPresent());
        assertEquals(email, user.get().getEmail());
    }

    @Test
    @Sql(scripts = "classpath:data/init_test_data.sql")
    void selectUserById_idNotExists_expectUserNotPresent() {
        //given
        var id = 31231;

        //when
        Optional<User> user = jdbcUserDao.selectUserById(id);

        //then
        assertFalse(user.isPresent());
    }

    @Test
    @Sql(scripts = "classpath:data/init_test_data.sql")
    void selectUserByEmail_emailExists_expectCorrectId() {
        //given
        var id = 1;
        var email = "test@mail.ru";

        //when
        Optional<User> user = jdbcUserDao.selectUserByEmail(email);

        //then
        assertTrue(user.isPresent());
        assertEquals(id, user.get().getId());
    }

    @Test
    @Sql(scripts = "classpath:data/init_test_data.sql")
    void selectUserByEmail_emailNotExists_expectUserNotPresent() {
        //given
        var email = "testsadfas@mail.ru";

        //when
        Optional<User> user = jdbcUserDao.selectUserByEmail(email);

        //then
        assertFalse(user.isPresent());
    }

    @Test
    @Sql(scripts = "classpath:data/init_test_data.sql")
    void changeUserRoleByUserId_userExists_expectChangedRole() {
        //given
        var userId = 1;
        Role expectedRole = Role.ADMIN;

        //when
        jdbcUserDao.changeUserRoleByUserId(userId, expectedRole);

        //then
        var user = jdbcUserDao.selectUserById(userId);
        assertTrue(user.isPresent());
        assertEquals(expectedRole, user.get().getRole());
    }
}