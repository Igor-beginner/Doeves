package md.brainet.doeves.verification;

import jakarta.validation.constraints.Size;

public record VerificationRequest(

        String email,

        @Size(min = 6, max = 6)
        String code
) {
}
