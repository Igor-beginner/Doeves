package md.brainet.doeves.mail;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;

public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;

    public MailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void send(MessageRequest message) {
        var simpleMail = new SimpleMailMessage();
        simpleMail.setTo(message.receiver());
        simpleMail.setSubject(message.topic());
        simpleMail.setSentDate(new Date());
        simpleMail.setText(message.content());
        javaMailSender.send(simpleMail);
    }
}
