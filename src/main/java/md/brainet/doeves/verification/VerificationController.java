package md.brainet.doeves.verification;

import jakarta.servlet.http.HttpServletRequest;
import md.brainet.doeves.jwt.JwtBlackListCache;
import md.brainet.doeves.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/verification")
public class VerificationController {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationController.class);

    private final VerificationService  verificationService;
    private final JwtBlackListCache jwtBlackListCache;

    public VerificationController(VerificationService verificationService,
                                  JwtBlackListCache jwtBlackListCache) {
        this.verificationService = verificationService;
        this.jwtBlackListCache = jwtBlackListCache;
    }

    @PostMapping
    public ResponseEntity<?> tryToVerify(
            HttpServletRequest request,
            @RequestParam String code,
            @AuthenticationPrincipal User user) {
        String verifiedJwtToken = verificationService.verify(user.getEmail(), code);
        String currentJwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        jwtBlackListCache.putJwtUntilDateExpired(currentJwtToken);

        LOG.info("Verified successfully [email={}]", user.getEmail());
        return ResponseEntity.accepted()
                .header(HttpHeaders.AUTHORIZATION, verifiedJwtToken)
                .body(new VerificationResponse(
                        "Account was verified successfully!",
                        verifiedJwtToken
                ));
    }

    @PostMapping("new")
    public ResponseEntity<?> sendNewVerificationCode(
            @AuthenticationPrincipal User user) {
        verificationService.generateNewCodeFor(user.getEmail());
        // TODO make code sending in other service
        LOG.info("Send new verification code for [email={}]", user.getEmail());
        return new ResponseEntity<>(
                new VerificationResponse(
                        "New code was sent to your email.",
                         null
                ),
                HttpStatus.OK
        );
    }
}
