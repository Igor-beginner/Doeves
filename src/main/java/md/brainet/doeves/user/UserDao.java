package md.brainet.doeves.user;

import java.util.Optional;

public interface UserDao {
    Optional<User> selectUserById(Integer userId);
    Optional<User> selectOwnerOfTaskWithId(int taskId);
    Integer insertUserAndDefaultRole(User user);
    Optional<User> selectUserByEmail(String email);
}
