package md.brainet.doeves.user;

import java.util.List;

public record UserDTO(
        Integer id,
        String email,
        Role role
) {
}
