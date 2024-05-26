package md.brainet.doeves.user;

import md.brainet.doeves.exception.UserNotFoundException;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class UserPermissionUtil {

    private final UserDao userDao;

    public UserPermissionUtil(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean haveEnoughRightsOver(User subUser) {
        Role singedRole = getSingedRole();
        return singedRole == Role.ADMIN && subUser.getRole() != Role.ADMIN;
    }

    public boolean haveEnoughRightsOver(Integer userId) {
        return haveEnoughRightsOver(
                userDao
                        .selectUserById(userId)
                        .orElseThrow(() ->
                                new UserNotFoundException((userId)))
        );
    }

    Role getSingedRole() {
        var principal = (org.springframework.security.core.userdetails.User)
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();
        var authorities = principal.getAuthorities();
        var authority = authorities.stream()
                .filter(a -> a.getAuthority().startsWith("ROLE_"))
                .findFirst()
                .orElseThrow();

        return Role.parse(authority);
    }
}
