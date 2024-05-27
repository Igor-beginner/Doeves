package md.brainet.doeves.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NewUserRequest(

        @Email(
                message = "Email [%s] isn't valid."
        )
        String email,

        @Pattern(
                 regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}",
                message = "Password is not valid!"
        )
        String password
) {
}
