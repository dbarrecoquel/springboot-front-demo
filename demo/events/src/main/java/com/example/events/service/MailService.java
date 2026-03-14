package com.example.events.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAbandonedBasketMail(String to, String basketId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Vous avez oublié des articles dans votre panier !");
        message.setText("""
            Bonjour,
            
            Vous avez laissé des articles dans votre panier (ID: %s).
            Revenez les commander avant qu'ils ne soient épuisés !
            
            À bientôt.
            """.formatted(basketId));
        mailSender.send(message);
        logger.info("Abandoned basket mail sent to {}", to);
    }
}