package com.fleetapp.identity.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;

import java.util.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Sends OTPs via SMTP using Jakarta Mail.
 *
 * Configured via {@code app.email.smtp.*} (env-overridable as {@code SMTP_HOST},
 * {@code SMTP_PORT}, {@code SMTP_USERNAME}, {@code SMTP_PASSWORD}, {@code SMTP_FROM}).
 * Selected when {@code app.email.provider=smtp}.
 */
@Service
@ConditionalOnProperty(name = "app.email.provider", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String from;

    public SmtpEmailSender(
            @Value("${app.email.smtp.host}") String host,
            @Value("${app.email.smtp.port}") int port,
            @Value("${app.email.smtp.username}") String username,
            @Value("${app.email.smtp.password}") String password,
            @Value("${app.email.smtp.from}") String from) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.from = from;
    }

    @Override
    public void send(String email, String otp) {
        if (host.isBlank() || username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "SMTP email sender is not configured: set app.email.smtp.host, "
                    + "app.email.smtp.username and app.email.smtp.password");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Your Yatrik verification code");
            message.setText("Your Yatrik verification code is " + otp + ". It expires in 5 minutes.");

            Transport.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("SMTP email send failed: " + ex.getMessage(), ex);
        }
    }
}