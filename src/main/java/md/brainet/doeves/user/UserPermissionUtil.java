package md.brainet.doeves.user;

import md.brainet.doeves.exception.PrincipalNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserPermissionUtil {

    private final UserDao userDao;

    public UserPermissionUtil(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean haveEnoughRightsOver(User subUser) {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return !isAdmin(subUser) && isAdmin(user);
    }

    public boolean haveEnoughRightsOver(Integer userId) {
        return haveEnoughRightsOver(
                userDao
                        .selectUserById(userId)
                        .orElseThrow(() -> new PrincipalNotFoundException("User with id [%s] cannot be found".formatted(userId)))
        );
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }
}
