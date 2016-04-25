package pl.agh.ochd.alerting;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.agh.ochd.model.LogSample;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.stream.Collectors;

public class AlertingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertingService.class);

    private final String SUBJECT = "[ALERTS] Host name: ";
    private final String BODY = "Pattern with name: %s matches: \n\n";
    private final String FROM;
    private final String USERNAME;
    private final String PASSWORD;

    private Address[] emails;
    private Properties properties;
    private Session session;

    public AlertingService(String emailAddress, String userName, String password, Collection<String> recipients) {

        FROM = emailAddress;
        USERNAME = userName;
        PASSWORD = password;
        properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        session = Session.getDefaultInstance(properties, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
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
            message.setFrom(new InternetAddress(FROM));
            message.addRecipients(Message.RecipientType.TO, emails);
            message.setSubject(SUBJECT + hostName);
            message.setText(messageBody);
        } catch (MessagingException e) {}

        return message;
    }

    private String prepareMessageBody(String patternKey, Collection<LogSample> alerts) {

        return String.format(BODY, patternKey) + String.join("\n", alerts.stream().map(LogSample::getMessage).collect(Collectors.toList()));
    }

    public void sendAlertNotification(String hostName, Collection<LogSample> alerts, String patternName) {

        LOGGER.debug("Sending email notification:\n\n " + prepareMessageBody(patternName, alerts));

        // FIXME uncomment
//        MimeMessage message = prepareMessage(hostName, prepareMessageBody(patternName, alerts));
//
//        try {
//            Transport.send(message);
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            // TODO
//        }
    }

    public static void main(String[] args) {

        List<String> emails = new ArrayList<>(Arrays.asList("ddszczygiel@gmail.com"));
        AlertingService alertingService = new AlertingService("ddszczygiel@gmail.com", "pass", "ddszczygiel@gmail.com", emails);
        alertingService.sendAlertNotification("dupa", Collections.emptyList(), "lala");
    }

}
