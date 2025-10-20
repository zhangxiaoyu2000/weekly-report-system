package com.weeklyreport.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Utility component for sending application emails from the default sender account.
 */
@Component
public class EmailSenderUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSenderUtil.class);
    private static final String DEFAULT_FROM_ADDRESS = "info@universebeyond.cn";

    private final JavaMailSender mailSender;

    public EmailSenderUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPlainText(String recipient, String subject, String content) {
        sendPlainText(Collections.singletonList(recipient), subject, content);
    }

    public void sendPlainText(Collection<String> recipients, String subject, String content) {
        String[] resolvedRecipients = resolveRecipients(recipients);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(DEFAULT_FROM_ADDRESS);
        message.setTo(resolvedRecipients);
        message.setSubject(subject);
        message.setText(content == null ? "" : content);

        try {
            mailSender.send(message);
            LOGGER.debug("Sent plain text email to {}", Arrays.toString(resolvedRecipients));
        } catch (MailException ex) {
            LOGGER.error("Failed to send plain text email to {}", Arrays.toString(resolvedRecipients), ex);
            throw ex;
        }
    }

    public void sendHtml(String recipient, String subject, String htmlContent) {
        sendHtml(Collections.singletonList(recipient), subject, htmlContent);
    }

    public void sendHtml(Collection<String> recipients, String subject, String htmlContent) {
        String[] resolvedRecipients = resolveRecipients(recipients);

        LOGGER.info("📨 准备发送HTML邮件: to={}, subject={}", Arrays.toString(resolvedRecipients), subject);

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_NO, StandardCharsets.UTF_8.name());
            helper.setFrom(DEFAULT_FROM_ADDRESS);
            helper.setTo(resolvedRecipients);
            helper.setSubject(subject);
            helper.setText(htmlContent == null ? "" : htmlContent, true);

            LOGGER.info("📨 开始发送邮件...");
            mailSender.send(mimeMessage);
            LOGGER.info("✅ HTML邮件发送成功: to={}", Arrays.toString(resolvedRecipients));
        } catch (MessagingException ex) {
            LOGGER.error("❌ 准备HTML邮件失败: to={}", Arrays.toString(resolvedRecipients), ex);
            throw new MailPreparationException("准备HTML邮件失败", ex);
        } catch (MailException ex) {
            LOGGER.error("❌ 发送HTML邮件失败: to={}, 原因={}", Arrays.toString(resolvedRecipients), ex.getMessage(), ex);
            throw ex;
        }
    }

    private String[] resolveRecipients(Collection<String> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("至少需要一个收件人邮箱地址");
        }

        List<String> sanitized = recipients.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .toList();

        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("至少需要一个有效的收件人邮箱地址");
        }

        return sanitized.toArray(String[]::new);
    }
}
