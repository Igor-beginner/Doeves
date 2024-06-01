package md.brainet.doeves.verification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> tryToVerify(@Valid @RequestBody VerificationRequest request) {
        verificationService.verify(request.email(), request.code());
        LOG.info("Verified successfully [email={}]", request.email());
        return new ResponseEntity<>(
                new VerificationResponse("Account was verified successfully!"),
                HttpStatus.ACCEPTED
        );
    }

    @PostMapping("new")
    public ResponseEntity<?> sendNewVerificationCode(
            @Valid @RequestParam @Email String email) {
        verificationService.sendNewCodeTo(email);
        LOG.info("Send new verification code for [email={}]", email);
        return new ResponseEntity<>(
                new VerificationResponse("New code was sent on your email."),
                HttpStatus.OK
        );
    }
}
