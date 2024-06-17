package md.brainet.doeves.mail;

import md.brainet.doeves.util.SpringActiveProfiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailSenderDevelopmentConfig {

    @Bean
    @Profile({"dev", "test"})
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl();
    }

    @Bean
    public MailService mailService(JavaMailSender javaMailSender,
                                   SpringActiveProfiles profiles) {
        return new MailServiceImpl(javaMailSender, profiles);
    }
}
