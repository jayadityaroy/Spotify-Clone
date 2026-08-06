package com.joy.spotify_clone.serviceImpl;

import com.joy.spotify_clone.service.EmailService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(EmailServiceImpl.class);
    @Autowired
    private JavaMailSender mailSender;
    @Value("${app.frontend.url:http://localhost:4200}") // from application.prop: default url
    private String frontendUrl;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendCredentialsEmail(String toEmail, String userName, String password) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Musify - Your Temporary Password");
            String emailBody =
                    "Hi " + userName + ",\n\n"
                        + "Welcome to the Spotify Clone API!\n\n"
                        + "Your temporary password is: " + password + "\n\n"
                        + "Please log in using this password and change it immediately for security reasons.\n\n"
                        + "You can log it at: " + frontendUrl + "/login\n\n"
                        + "If you didn't request a password reset, please ignore this email.\n\n"
                        + "Best regards,\n"
                        + "The Spotify Clone Team";
            message.setText(emailBody);
            mailSender.send(message);
            logger.info("Temporary password email sent to {}", toEmail);

        }catch(Exception e){
            logger.error("Failed to send temporary password email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send temporary password email");
        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String userName, String password) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Musify!");
            String emailBody =
                    "Hi " + userName + ",\n\n"
                        + "Welcome to the Spotify Clone API!\n\n"
                        + "Your account has been created successfully.\n\n"
                        + "You can log in at: " + frontendUrl + "/login\n\n"
                        + "Your temporary password is: " + password + "\n\n"
                        + "Please log in using this password and change it immediately for security reasons.\n\n"
                        + "If you didn't request this account, please ignore this email.\n\n"
                        + "Best regards,\n"
                        + "The Spotify Clone Team";
            message.setText(emailBody);
            mailSender.send(message);
            logger.info("Welcome email sent to {}", toEmail);

        }catch(Exception e){
            logger.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send welcome email");
        }
    }
}

/*
line 17: {$app.frontend.url:http://localhost:4200} means:
- app.frontend.url is the property name in application.properties
- http://localhost:4200 is the default value if the property is not set
 */


