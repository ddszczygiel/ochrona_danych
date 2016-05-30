package pl.agh.ochd.alerting;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.agh.ochd.model.LogSample;
import pl.agh.ochd.model.NotificationData;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.stream.Collectors;

public class AlertingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertingService.class);

    private static final String SUBJECT = "[ALERTS] Host name: ";
    private static final String BODY = "Pattern with name: %s matches: \n\n";
    private final String from;
    private final String userName;
    private final String password;
    private final String smtpHost;
    private final String smtpPort;

    private Address[] emails;
    private Properties properties;
    private Session session;

    public AlertingService(String emailAddress, String userName, String password, String smtpHost, String smtpPort, Collection<String> recipients) {

        this.from = emailAddress;
        this.userName = userName;
        this.password = password;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;

        properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);

        session = Session.getDefaultInstance(properties, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(AlertingService.this.userName, AlertingService.this.password);
            }
        });
        prepareRecipients(recipients);
    }

    private void prepareRecipients(Collection<String> recipients) {

        emails = new Address[recipients.size()];
        final int[] i = {0};
        recipients.forEach(recipient -> {
            try {
                emails[i[0]++] = new InternetAddress(recipient);
            } catch (AddressException e) {
                // should not happen
                e.printStackTrace();
                throw new IllegalStateException("Can not parse email address: " + recipient);
            }
        });
    }

    private MimeMessage prepareMessage(String hostName, String messageBody) {

        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(from));
            message.addRecipients(Message.RecipientType.TO, emails);
            message.setSubject(SUBJECT + hostName);
            message.setText(messageBody);
        } catch (MessagingException e) {
        }

        return message;
    }

    private String prepareMessageBody(String patternKey, Collection<LogSample> alerts) {

        return String.format(BODY, patternKey) + String.join("\n", alerts.stream().map(LogSample::getMessage).collect(Collectors.toList()));
    }

    public void sendAlertNotification(NotificationData notificationData) {

        LOGGER.debug("Sending email notification:\n\n " + prepareMessageBody(notificationData.getPatternName(), notificationData.getMatched()));
        MimeMessage message = prepareMessage(notificationData.getHostName(),
                prepareMessageBody(notificationData.getPatternName(), notificationData.getMatched()));
        try {
            Transport.send(message);
        } catch (MessagingException e) {
            LOGGER.error("Could not send email notification", e);
        }
    }
}


