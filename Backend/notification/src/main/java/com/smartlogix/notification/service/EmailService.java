package com.smartlogix.notification.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${mail.from:noreply@smartlogix.cl}")
    private String from;

    @CircuitBreaker(name = "emailService", fallbackMethod = "fallbackSendEmail")
    public void sendHtmlEmail(String to, String subject, String templateName, Context ctx) {
        if (to == null || to.isBlank()) {
            log.warn("Email no enviado — destinatario nulo para asunto: {}", subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(templateEngine.process("email/" + templateName, ctx), true);
            mailSender.send(message);
            log.info("Email enviado a {} — asunto: {}", to, subject);
        } catch (MessagingException e) {
            throw new RuntimeException("Error preparando email a " + to + ": " + e.getMessage(), e);
        }
    }

    void fallbackSendEmail(String to, String subject, String templateName, Context ctx, Throwable t) {
        log.warn("Email no enviado a {} (circuito abierto o fallo SMTP) — asunto: {} — causa: {}",
                to, subject, t.getMessage());
    }
}
