package com.grupoq.app.models.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailSenderService {

    @Autowired
    private JavaMailSender javamailsender;

    public void sendEmail(String to, String body, String topic) {
        SimpleMailMessage simplemailmessage = new SimpleMailMessage();
        simplemailmessage.setFrom("dysumardevapp@gmail.com");
        simplemailmessage.setTo(to);
        simplemailmessage.setSubject(topic);
        simplemailmessage.setText(body);
        javamailsender.send(simplemailmessage);
        System.out.print("\nSending an Email");
    }

    public void sendEmailchris(String body, String topic) {
        SimpleMailMessage simplemailmessage = new SimpleMailMessage();
        simplemailmessage.setFrom("dysumardevapp@gmail.com");
        simplemailmessage.setTo("cristofr5248@gmail.com");
        simplemailmessage.setSubject(topic);
        simplemailmessage.setText(body);
        javamailsender.send(simplemailmessage);
        System.out.print("\nSending an Email");
    }
}