package md.brainet.doeves.user;

import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final NewUserRequestMapper requestMapper;

    public UserServiceImpl(UserDao userDao,
                           NewUserRequestMapper requestMapper) {
        this.userDao = userDao;
        this.requestMapper = requestMapper;
    }

    @Override
    public Integer makeUser(NewUserRequest request) {
        return userDao.insertUserAndDefaultRole(requestMapper.apply(request));
    }

    @Override
    public User findUser(Integer userId) {
        return userDao.selectUserById(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "User with id [%s] doesn't exist".formatted(userId)
                        )
                );
    }

    @Override
    public void disableUser(Integer userId) {
        throw new RuntimeException("While it isn't implemented");
    }

    @Override
    public void enableUser(Integer userId) {
        throw new RuntimeException("While it isn't implemented");
    }

    @Override
    public void removeUser(Integer userId) {
        throw new RuntimeException("While it isn't implemented");
    }

    @Override
    public void addAuthority(Role role, Integer userId) {
        throw new RuntimeException("While it isn't implemented");
    }

    @Override
    public void removeAuthority(Role role, Integer userId) {
        throw new RuntimeException("While it isn't implemented");
    }

    @Override
    public void changePassword(String newPassword, Integer userId) {
        throw new RuntimeException("While it isn't implemented");
    }
}
