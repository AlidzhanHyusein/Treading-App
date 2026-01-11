package com.alidzhan.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private JavaMailSender mailSender;

    public void SendVerificationOtpEmail(String email,String otp) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,"utf-8");

        String subject="Verification OTP";
        String text = "Your verification code is "+otp;

        helper.setSubject(subject);
        helper.setText(text);
        helper.setTo(email);

        try {
            mailSender.send(mimeMessage);
        } catch (MailException e) {
            throw new RuntimeException("email sending failed...");
        }
    }
}
