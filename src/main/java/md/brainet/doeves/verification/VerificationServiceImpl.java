package md.brainet.doeves.verification;

import md.brainet.doeves.exception.*;
import md.brainet.doeves.jwt.JWTUtil;
import md.brainet.doeves.mail.MailService;
import md.brainet.doeves.mail.MessageRequest;
import md.brainet.doeves.user.User;
import md.brainet.doeves.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationServiceImpl implements VerificationService {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationController.class);
    private static final String VERIFICATION_MESSAGE_SUBJECT = "Verification code:";
    private static final String VERIFICATION_MESSAGE_CONTENT = "Your code: %s. Please, don't give it other persons";

    private final VerificationDetailsDao verificationDao;
    private final UserService userService;
    private final MailService mailService;
    private final CodeGenerator codeGenerator;
    private final JWTUtil jwtUtil;

    public VerificationServiceImpl(
            VerificationDetailsDao verificationDao,
            UserService userService,
            MailService mailService,
            CodeGenerator codeGenerator,
            JWTUtil jwtUtil) {
        this.verificationDao = verificationDao;
        this.userService = userService;
        this.mailService = mailService;
        this.codeGenerator = codeGenerator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            noRollbackFor = VerificationBadCodeException.class
    )
    public String verify(String email, String code) {

        if(verificationDao.isUserVerified(email)) {
            throw new VerificationException(
                    "User with email %s already verified".formatted(email)
            );
        }

        VerificationDetails details =
                verificationDao.decrementVerificationDetailsAttemptByEmail(email)
                        .orElseThrow(VerificationCodeIsEmptyException::new);


        if (LocalDateTime.now().isAfter(details.getExpireDate())
                || details.getMissingAttempts() < 0) {
            throw new VerificationCodeExpiredException(email, code);
        } else if (!details.getCode().equals(code)) {
            throw new VerificationBadCodeException(email, code);
        }

        verificationDao.verifyUserByEmail(email);

        return issueVerifiedTokenForUserEmail(email);
    }

    private String issueVerifiedTokenForUserEmail(String email) {
        User user = userService.findUser(email);
        return jwtUtil.issueTokenWithRoles(
                user.getEmail(),
                List.of(user.getRole()),
                user.isVerified()
        );
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void generateNewCodeFor(String email) {
        VerificationDetails details = VerificationDetailsFactory
                .build(codeGenerator);

        User user = userService.findUser(email);

        if(user.getVerificationDetailsId() == 0) {
            Integer detailsId = verificationDao.insertVerificationDetails(details);
            verificationDao.updateVerificationDetails(email, detailsId);
        }else {
            verificationDao.updateVerificationDetails(email, details);
        }
        //todo return verification code and invoke method to send message from controller
        sendVerificationMessage(email, details.getCode());

        LOG.debug("Verification code for {} is {}", email, details.getCode());
    }

    //todo extract method using Factory pattern
    private void sendVerificationMessage(String email, String code) {
        MessageRequest messageRequest = new MessageRequest(
                email,
                VERIFICATION_MESSAGE_SUBJECT,
                VERIFICATION_MESSAGE_CONTENT.formatted(code)
        );
//        MessageRequest request = MessageRequestFactory.build(MessageType.VERIFICATION_CONFIRMATION, email);
        mailService.send(messageRequest);
    }
}
