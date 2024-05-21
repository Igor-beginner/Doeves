package md.brainet.doeves.auth;

import md.brainet.doeves.user.NewUserRequest;
import md.brainet.doeves.user.Role;
import md.brainet.doeves.user.UserDTO;

import java.util.List;

public record AuthenticationResponse (
        String token,
        UserDTO userDTO){
}
