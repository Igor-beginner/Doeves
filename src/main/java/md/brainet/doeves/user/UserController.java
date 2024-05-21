package md.brainet.doeves.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("make")
    public ResponseEntity<NewUserResponse> makeNewUser(
            @RequestBody @Valid NewUserRequest userDTO) {

        Integer userId = userService.makeUser(userDTO);
        return ResponseEntity.ok(new NewUserResponse(
                userId,
                "User was saved successfully"
        ));
    }

    public record NewUserResponse (
            Integer id,
            String message
    ) {}
}
