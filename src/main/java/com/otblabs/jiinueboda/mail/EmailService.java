package com.otblabs.jiinueboda.mail;

import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {


    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an email with CSV file attachment
     * @param csvContent The CSV content as a string
     * @param fileName Name of the CSV file
     * @param toEmail Destination email address
     * @param subject Email subject
     * @throws MessagingException if email sending fails
     */
    public void sendEmailWithCSVAttachment(String csvContent, String fileName, String toEmail, String subject)
            throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);

        // Build email body
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<html><body>");
        emailBody.append("<h2>").append(subject).append("</h2>");
        emailBody.append("<p><strong>Generated:</strong> ").append(timestamp).append("</p>");
        emailBody.append("<p>Please find the attached CSV file with updated data.</p>");
        emailBody.append("</body></html>");

        helper.setText(emailBody.toString(), true);

        // Attach CSV file using ByteArrayDataSource
//        DataSource dataSource = new ByteArrayDataSource(csvContent.getBytes(StandardCharsets.UTF_8), "text/csv");
//        helper.addAttachment(fileName, dataSource);

        DataSource dataSource = new ByteArrayDataSource(csvContent.getBytes(StandardCharsets.UTF_8), "application/octet-stream");
        helper.addAttachment(fileName, dataSource);


//        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
//        helper.addAttachment(fileName, new ByteArrayResource(inputStream.readAllBytes()), "text/csv");


        mailSender.send(message);
    }

    /**
     * Overloaded method with default filename
     */
    public void sendEmailWithCSVAttachment(String csvContent, String toEmail, String subject)
            throws MessagingException, UnsupportedEncodingException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = subject.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".csv";
        sendEmailWithCSVAttachment(csvContent, fileName, toEmail, subject);
    }
}