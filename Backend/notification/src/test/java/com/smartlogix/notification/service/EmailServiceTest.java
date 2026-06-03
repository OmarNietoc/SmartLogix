package com.smartlogix.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock SpringTemplateEngine templateEngine;
    @InjectMocks EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "from", "noreply@smartlogix.cl");
    }

    @Test
    void sendHtmlEmail_validRecipient_sendsEmail() throws MessagingException {
        // arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        Context ctx = new Context();

        // act
        emailService.sendHtmlEmail("user@empresa.cl", "Pedido creado", "order-created", ctx);

        // assert
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("email/order-created"), any(Context.class));
    }

    @Test
    void sendHtmlEmail_nullRecipient_skipsWithoutSending() {
        // arrange
        Context ctx = new Context();

        // act
        emailService.sendHtmlEmail(null, "Asunto", "template", ctx);

        // assert
        verifyNoInteractions(mailSender);
        verifyNoInteractions(templateEngine);
    }

    @Test
    void sendHtmlEmail_blankRecipient_skipsWithoutSending() {
        // arrange
        Context ctx = new Context();

        // act
        emailService.sendHtmlEmail("   ", "Asunto", "template", ctx);

        // assert
        verifyNoInteractions(mailSender);
    }

    @Test
    void sendHtmlEmail_messagingException_wrapsInRuntimeException() throws MessagingException {
        // arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));
        Context ctx = new Context();

        // act & assert
        assertThatThrownBy(() -> emailService.sendHtmlEmail("user@empresa.cl", "Asunto", "template", ctx))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void fallbackSendEmail_logsWarningWithoutThrowing() {
        // arrange
        Context ctx = new Context();
        RuntimeException cause = new RuntimeException("Circuit open");

        // act — fallback should not throw
        emailService.fallbackSendEmail("user@empresa.cl", "Asunto", "template", ctx, cause);

        // assert
        verifyNoInteractions(mailSender);
    }
}
