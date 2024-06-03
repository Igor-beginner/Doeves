package md.brainet.doeves.user;

import md.brainet.doeves.exception.VerificationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.selectUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                                "Email '%s' doesn't exist".formatted(username)
                        )
                );

        return user;
    }
}
