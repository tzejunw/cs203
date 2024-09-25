package com.java.firebase.demo.user;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    public void sendVerificationEmail(String recipientEmail, String verificationLink) {
        try {
            // Obtain the mail session from EmailConfig
            Session session = EmailConfig.getSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("magicarena148@gmail.com")); // Your email
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Verify Your Email Address");
            message.setText("Please click the following link to verify your email: " + verificationLink);

            Transport.send(message);
            System.out.println("Verification email sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
