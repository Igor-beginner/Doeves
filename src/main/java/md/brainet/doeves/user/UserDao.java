package md.brainet.doeves.user;

import java.util.Optional;

public interface UserDao {
    Optional<User> selectUserById(Integer userId);
    Optional<User> selectOwnerOfTaskWithId(Integer taskId);
    Integer insertUserAndDefaultRole(User user);
    Optional<User> selectUserByEmail(String email);
    void changeUserRoleByUserId(Integer userId, Role role);
}
