package md.brainet.doeves.verification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import md.brainet.doeves.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/verification")
public class VerificationController {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationController.class);

    private final VerificationService  verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping
    public ResponseEntity<?> tryToVerify(
            @RequestParam String code,
            @AuthenticationPrincipal User user) {
        verificationService.verify(user.getEmail(), code);
        LOG.info("Verified successfully [email={}]", user.getEmail());
        return new ResponseEntity<>(
                new VerificationResponse("Account was verified successfully!"),
                HttpStatus.ACCEPTED
        );
    }

    @PostMapping("new")
    public ResponseEntity<?> sendNewVerificationCode(
            @AuthenticationPrincipal User user) {
        verificationService.generateNewCodeFor(user.getEmail());
        // TODO make code sending in other service
        LOG.info("Send new verification code for [email={}]", user.getEmail());
        return new ResponseEntity<>(
                new VerificationResponse("New code was sent to your email."),
                HttpStatus.OK
        );
    }
}
