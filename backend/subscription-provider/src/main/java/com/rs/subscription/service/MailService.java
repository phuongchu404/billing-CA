package com.rs.subscription.service;

import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import java.util.Properties;

public interface MailService {

    void send(String to, String subject, String body);

    void sendTestEmail(String to);
}
