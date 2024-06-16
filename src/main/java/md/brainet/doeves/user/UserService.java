package md.brainet.doeves.user;

import java.util.Optional;

public interface UserService {

    User findUser(Integer userId);

    User findUser(String email);
    void disableUser(Integer userId);
    void enableUser(Integer userId);
    Integer makeUser(NewUserRequest userDTO);
    void removeUser(Integer userId);
    void addAuthority(Role role, Integer userId);
    void removeAuthority(Role role, Integer userId);
    void changePassword(String newPassword, Integer userId);
}
