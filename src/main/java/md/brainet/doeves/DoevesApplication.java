package md.brainet.doeves;

import jakarta.annotation.PostConstruct;
import org.apache.juli.logging.Log;
import org.eclipse.angus.mail.smtp.SMTPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;

@SpringBootApplication
@EnableTransactionManagement
public class DoevesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoevesApplication.class, args);
    }
}
