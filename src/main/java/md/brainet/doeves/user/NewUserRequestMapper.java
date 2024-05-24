package md.brainet.doeves.user;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class NewUserRequestMapper implements Function<NewUserRequest, User> {

    @Override
    public User apply(NewUserRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(request.password());
        return user;
    }
}
