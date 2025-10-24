package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.exceptions.SendMailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
@Slf4j
public class MailSenderService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Context context) {
        log.info("Отправка HTML-письма (Thymeleaf) на: {}", to);
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            String htmlContent = templateEngine.process(templateName, context);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            log.info("HTML-письмо успешно отправлено на: {}", to);

        } catch (MessagingException e) {
            log.error("Ошибка при отправке HTML-письма на: {}. Причина: {}", to, e.getMessage());
            throw new SendMailException("Не удалось отправить HTML-письмо с помощью Thymeleaf");
        }
    }
}