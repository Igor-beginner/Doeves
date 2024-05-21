package md.brainet.doeves.user;

import java.util.Optional;

public interface UserDao {
    Optional<User> selectUserById(Integer userId);
    User selectOwnerOfTaskWithId(int taskId);
    Integer insertUser(User user);
    Optional<User> selectUserByEmail(String email);
}
