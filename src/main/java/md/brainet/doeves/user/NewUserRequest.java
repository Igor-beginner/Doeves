package md.brainet.doeves.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

// TODO validate
public record NewUserRequest(

        @Email(
                message = "Email is not valid!"
        )
        String email,

        @Size(
                min = 4,
                max = 15,
                message = "Password must be no less than 4 characters and no more than 15 characters"
        )
        String password
) {
}
