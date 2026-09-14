package PlanIT.PlanIT.service;

import PlanIT.PlanIT.config.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
@RequiredArgsConstructor
@Slf4j

public class EmailService {

    private final JavaMailSender javaMailSender;
    private final AppProperties appProperties;
    private final JavaMailSenderImpl mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendActivationEmail(String to, String name, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Activate Your Account");

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("activationUrl", baseUrl + "/api/v2.0/auth/activate?token=" + token);

            String htmlContent = templateEngine.process("activation-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Activation email sent successfully to: {}", to);


        } catch (MessagingException e) {
            log.error("Failed to send activation email to: {}", to, e);
            throw new RuntimeException("Failed to send activation email", e);
        }


    }

    public void sendPasswordResetEmail(String email, String fullName, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Password Reset Request");

            String resetLink = "http://localhost:3000/auth/reset-password?token=" + resetToken;

            // Create Thymeleaf context with variables
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("resetLink", resetLink);
            context.setVariable("resetToken", resetToken);

            String htmlContent = templateEngine.process("password-reset-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new MailException("Failed to send password reset email", e) {};
        }
    }
}