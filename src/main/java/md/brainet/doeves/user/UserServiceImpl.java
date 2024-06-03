package md.brainet.doeves.user;

import md.brainet.doeves.exception.EmailAlreadyExistsDaoException;
import md.brainet.doeves.verification.VerificationService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final NewUserRequestMapper requestMapper;
    private final VerificationService verificationService;

    public UserServiceImpl(UserDao userDao,
                           NewUserRequestMapper requestMapper,
                           VerificationService verificationService) {
        this.userDao = userDao;
        this.requestMapper = requestMapper;
        this.verificationService = verificationService;
    }

    @Override
    public Integer makeUser(NewUserRequest request) {

        try {

            //TODO user must not receive verification code if already exists
            // method insertUserAndDefaultRole(user) must invoked earlier

            //TODO make method for sending email as asynch
            User user = requestMapper.apply(request);
            Integer verificationDetailsId =
                    verificationService
                            .generateVerificationDetailsFor(request.email());
            user.setVerificationDetailsId(verificationDetailsId);
            Integer id = userDao.insertUserAndDefaultRole(user);
            return id;
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
