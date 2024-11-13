package com.java.firebase.demo.user;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    // @Value("${EMAIL_SENDER_USERNAME}")
    private String EMAIL_SENDER_USERNAME = "magicarena431@gmail.com";

    public void sendVerificationEmail(String recipientEmail, String verificationLink) {
        String htmlContent = "<!DOCTYPE html>"
                + "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<style> body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }"
                + ".container { background-color: #ffffff; max-width: 600px; margin: 40px auto; padding: 20px; border-radius: 10px;"
                + "box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }"
                + ".header { text-align: center; background-color: #007BFF; color: white; padding: 10px 0; border-radius: 10px 10px 0 0; }"
                + ".header h1 { margin: 0; } .content { padding: 20px; text-align: center; }"
                + ".content p { font-size: 16px; line-height: 1.6; color: #333333; } .button { display: inline-block;"
                + "padding: 12px 24px; background-color: #007BFF; color: white; text-decoration: none;"
                + "border-radius: 5px; font-weight: bold; }"
                + ".footer { text-align: center; font-size: 12px; color: #999999; margin-top: 20px; }</style>"
                + "</head><body><div class='container'><div class='header'><h1>Email Confirmation</h1></div>"
                + "<div class='content'><p>Hello,</p><p>Thank you for signing up! Please confirm your email address"
                + " by clicking the button below:</p>"
                + "<a href='[confirmation_link]' class='button'>Confirm Email</a>"
                + "<p>If you did not sign up for this account, please ignore this email.</p></div>"
                + "<div class='footer'><p>Magic: The Gathering 2024 All rights reserved.</p></div></div></body></html>";

        // Replace placeholders with actual content
        htmlContent = htmlContent.replace("[confirmation_link]", verificationLink);

        try {

            // Load environment variables
            // Dotenv dotenv = Dotenv.load();

            // Get the Base64 encoded key from environment variable
            // String EMAIL_SENDER_USERNAME = dotenv.get("EMAIL_SENDER_USERNAME");

            // Obtain the mail session from EmailConfig
            Session session = EmailConfig.getSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_SENDER_USERNAME)); 
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Verify Your Email Address");
            message.setContent(htmlContent, "text/html; charset=utf-8");
            // message.setText();

            Transport.send(message);
            System.out.println("Verification email sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
