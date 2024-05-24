package md.brainet.doeves.user;

import md.brainet.doeves.exception.EmailAlreadyExistsDaoException;
import org.springframework.dao.DuplicateKeyException;
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
        try {
            return userDao.insertUserAndDefaultRole(
                            requestMapper.apply(request)
                    );
        } catch (DuplicateKeyException e) {
            throw new EmailAlreadyExistsDaoException(
                    "Email [%s] already exists."
                            .formatted(request.email()),
                    e
            );
        }
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
