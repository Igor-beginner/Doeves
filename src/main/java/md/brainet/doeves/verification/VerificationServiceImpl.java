package md.brainet.doeves.verification;

import md.brainet.doeves.exception.UserNotFoundException;
import md.brainet.doeves.exception.VerificationBadCodeException;
import md.brainet.doeves.exception.VerificationCodeExpiredException;
import md.brainet.doeves.exception.VerificationException;
import md.brainet.doeves.mail.MailService;
import md.brainet.doeves.mail.MessageRequest;
import md.brainet.doeves.user.User;
import md.brainet.doeves.user.UserDao;
import md.brainet.doeves.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VerificationServiceImpl implements VerificationService {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationController.class);
    private static final String VERIFICATION_MESSAGE_SUBJECT = "Verification code:";
    private static final String VERIFICATION_MESSAGE_CONTENT = "Your code: %s. Please, don't give it other persons";

    private final VerificationDetailsDao verificationDao;
    private final UserService userService;
    private final MailService mailService;
    private final CodeGenerator codeGenerator;

    public VerificationServiceImpl(
            VerificationDetailsDao verificationDao,
            UserService userService,
            MailService mailService,
            CodeGenerator codeGenerator) {
        this.verificationDao = verificationDao;
        this.userService = userService;
        this.mailService = mailService;
        this.codeGenerator = codeGenerator;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void verify(String email, String code) {

        if(verificationDao.isUserVerified(email)) {
            throw new VerificationException(
                    "User with email %s already verified".formatted(email)
            );
        }

        VerificationDetails details =
                verificationDao.selectVerificationDetailsByEmail(email)
                        .orElseThrow(() -> new UserNotFoundException(email));

        if (LocalDateTime.now().isAfter(details.getExpireDate())
                || details.getMissingAttempts() <= 0) {
            throw new VerificationCodeExpiredException(email, code);
        } else if (!details.getCode().equals(code)) {
            throw new VerificationBadCodeException(email, code);
        }

        verificationDao.verifyUserByEmail(email);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void generateNewCodeFor(String email) {
        VerificationDetails details = VerificationDetailsFactory
                .build(codeGenerator);

        User user = userService.findUser(email);

        if(user.getVerificationDetailsId() == null) {
            Integer detailsId = verificationDao.insertVerificationDetails(details);
            verificationDao.updateUserVerificationDetailsId(email, detailsId);
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
