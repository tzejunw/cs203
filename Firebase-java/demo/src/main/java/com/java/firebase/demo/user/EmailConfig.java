package com.java.firebase.demo.user;

import java.util.Properties;

import javax.mail.Session;

// import io.github.cdimascio.dotenv.Dotenv;

public class EmailConfig {
    // @Value("${EMAIL_SENDER_USERNAME}")
    private static String EMAIL_SENDER_USERNAME = "magicarena431@gmail.com";

    // @Value("${EMAIL_SENDER_PASSWORD}")
    private static String EMAIL_SENDER_PASSWORD = "szhp bltz wcom bbee";

    public static Session getSession() {

         // Load environment variables
        // Dotenv dotenv = Dotenv.load();

        // Get the Base64 encoded key from environment variable
        // String EMAIL_SENDER_USERNAME = dotenv.get("EMAIL_SENDER_USERNAME");
        // String EMAIL_SENDER_PASSWORD = dotenv.get("EMAIL_SENDER_PASSWORD");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com"); 
        props.put("mail.smtp.port", "587"); 

        final String username = EMAIL_SENDER_USERNAME; 
        final String password = EMAIL_SENDER_PASSWORD;

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(username, password);
            }
        });

        return session;
    }
}
