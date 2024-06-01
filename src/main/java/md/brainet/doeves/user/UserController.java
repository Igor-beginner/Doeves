package md.brainet.doeves.user;

import jakarta.validation.Valid;
import md.brainet.doeves.auth.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/user")
public class UserController {
    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<NewUserResponse> makeNewUser(
            @RequestBody @Valid NewUserRequest userDTO) {

        Integer userId = userService.makeUser(userDTO);
        LOG.info("New reg request [{}]. User was created, id -> [{}]", userDTO.email(), userId);
        return new ResponseEntity<>(new NewUserResponse(
                userId,
                "User was saved successfully"
        ), HttpStatus.CREATED);
    }
}
