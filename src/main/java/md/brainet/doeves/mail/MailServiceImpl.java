package md.brainet.doeves.mail;

import md.brainet.doeves.util.Profile;
import md.brainet.doeves.util.SpringActiveProfiles;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;

public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;
    private final SpringActiveProfiles profiles;

    public MailServiceImpl(JavaMailSender javaMailSender,
                           SpringActiveProfiles profiles) {
        this.javaMailSender = javaMailSender;
        this.profiles = profiles;
    }

    @Override
    public void send(MessageRequest message) {
        if(!profiles.contains(Profile.PRODUCTION)) {
            return;
        }
        var simpleMail = new SimpleMailMessage();
        simpleMail.setTo(message.receiver());
        simpleMail.setSubject(message.topic());
        simpleMail.setSentDate(new Date());
        simpleMail.setText(message.content());
        javaMailSender.send(simpleMail);
    }
}

