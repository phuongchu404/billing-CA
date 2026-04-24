package com.rs.subscription.service;

import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final SystemSettingService settings;

    public void send(String to, String subject, String body) {
        if (!settings.getBoolean("email.enabled")) {
            log.debug("Email sending disabled — skipping message to {}", to);
            return;
        }
        doSend(to, subject, body);
    }

    /** Always sends regardless of the enabled flag — used for test-email. */
    public void sendTestEmail(String to) {
        String host = settings.get("email.host", null);
        if (host == null || host.isBlank()) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "SMTP host is not configured", 400);
        }
        doSend(to, "Test Email — Subscription Management", "This is a test email from Subscription Management.\nIf you received this, email configuration is working correctly.");
    }

    private void doSend(String to, String subject, String body) {
        JavaMailSenderImpl sender = buildSender();
        SimpleMailMessage msg = new SimpleMailMessage();
        String fromName    = settings.get("email.from.name", "Subscription Management");
        String fromAddress = settings.get("email.from.address", "noreply@example.com");
        msg.setFrom(fromName + " <" + fromAddress + ">");
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        try {
            sender.send(msg);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new SmsException(ErrorCodes.INTERNAL_ERROR, "Failed to send email: " + e.getMessage(), 500);
        }
    }

    private JavaMailSenderImpl buildSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(settings.get("email.host", "localhost"));
        sender.setPort(settings.getInt("email.port", 587));
        sender.setUsername(settings.get("email.username", ""));
        sender.setPassword(settings.get("email.password", ""));

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        if (settings.getBoolean("email.ssl.enabled")) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return sender;
    }
}
