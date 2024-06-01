package md.brainet.doeves;

import md.brainet.doeves.mail.MailService;
import md.brainet.doeves.mail.MailServiceImpl;
import md.brainet.doeves.mail.MessageRequest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestConfig {

    @Bean
    public MailService mailService() {
        return message -> {
            System.out.println("Emulation of sending to " + message.receiver());
        };
    }
}

