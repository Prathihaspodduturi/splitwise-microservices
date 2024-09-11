package com.prathihassplitwise.email.functions;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.function.Consumer;

@Configuration
public class EmailFunctions {

    private static final Logger log = LoggerFactory.getLogger(EmailFunctions.class);

    // Inject SendGrid API key from the application properties or environment variables
    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Bean
    public Consumer<String> email() {

        return userEmail -> {
            log.info("Sending email to: " + userEmail);

            // Create the SendGrid client using your API key
            SendGrid sendGrid = new SendGrid(sendGridApiKey);

            // Define the sender and receiver email addresses
            Email from = new Email("no-reply@splitsy.prathihaspodduturi.tech");
            Email to = new Email(userEmail); // The recipient's email passed as a String

            // the content of the email (HTML with the new GIF)
            Content content = new Content("text/html",
                    "<h1>Welcome to Splitsy!</h1>" +
                            "<p>Your account has been created successfully.</p>" +
                            "<img src='https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExbzYxdWd4d3FqbDdmMWQzeDBjeXBoc2twMW1heW04ZHp4ajNhdWE2bSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/5UA8yzZgQeq3C02eA2/giphy.gif' alt='Welcome GIF' />" +
                            "<p>We are thrilled to have you as part of our community!</p>");

            // Build the Mail object with the subject, from, to, and content
            Mail mail = new Mail(from, "Welcome to Splitsy!", to, content);

            // Create the request to SendGrid's API
            Request request = new Request();
            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());

                // Send the email using the SendGrid API
                Response response = sendGrid.api(request);

                // Log the response details (status code, body, headers)
                log.info("Status code: " + response.getStatusCode());
                log.info("Body: " + response.getBody());
                log.info("Headers: " + response.getHeaders());

            } catch (IOException e) {
                log.error("Error sending email: " + e.getMessage());
            }
        };
    }
}
