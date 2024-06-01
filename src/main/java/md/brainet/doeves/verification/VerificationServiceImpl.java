package md.brainet.doeves.verification;

import md.brainet.doeves.exception.UserNotFoundException;
import md.brainet.doeves.exception.VerificationException;
import md.brainet.doeves.mail.MailService;
import md.brainet.doeves.mail.MessageRequest;
import md.brainet.doeves.mail.MessageRequestFactory;
import md.brainet.doeves.mail.MessageType;
import md.brainet.doeves.user.UserDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VerificationServiceImpl implements VerificationService {

    private static final String VERIFICATION_MESSAGE_SUBJECT = "Verification code:";
    private static final String VERIFICATION_MESSAGE_CONTENT = "Your code: %s. Please, don't give it other persons";

    private final VerificationDetailsDao verificationDao;
    private final UserDao userDao;
    private final MailService mailService;
    private final CodeGenerator codeGenerator;

    public VerificationServiceImpl(
            VerificationDetailsDao verificationDao,
            UserDao userDao,
            MailService mailService,
            CodeGenerator codeGenerator) {
        this.verificationDao = verificationDao;
        this.userDao = userDao;
        this.mailService = mailService;
        this.codeGenerator = codeGenerator;
    }

    @Override
    @Transactional
    public void verify(String email, String code) {
        VerificationDetails details =
                verificationDao.selectVerificationDetailsByEmail(email)
                        .orElseThrow(() -> new UserNotFoundException(email));

        if (LocalDateTime.now().isAfter(details.getExpireDate())
                || details.getMissingAttempts() == 0) {
            sendNewCodeTo(email);
            throw new VerificationException(email, code);
        } else if (!details.getCode().equals(code)) {
            throw new VerificationException(email, code);
        }

        userDao.verifyUserByEmail(email);
    }

    @Override
    public void sendNewCodeTo(String email) {
        VerificationDetails details = VerificationDetailsFactory
                .build(codeGenerator);

        boolean updated = verificationDao.updateVerificationDetails(email, details);

        if (!updated) {
            throw new UserNotFoundException(email);
        }

        sendVerificationMessage(email, details.getCode());
    }

    private void sendVerificationMessage(String email, String code) {
        MessageRequest messageRequest = new MessageRequest(
                email,
                VERIFICATION_MESSAGE_SUBJECT,
                VERIFICATION_MESSAGE_CONTENT.formatted(code)
        );
//        MessageRequest request = MessageRequestFactory.build(MessageType.VERIFICATION_CONFIRMATION, email);
        mailService.send(messageRequest);
    }


    @Override
    public Integer generateVerificationDetailsFor(String email) {
        VerificationDetails details = VerificationDetailsFactory.build(codeGenerator);
        Integer id = verificationDao.insertVerificationDetails(details);
        sendVerificationMessage(email, details.getCode());
        return id;
    }
}
