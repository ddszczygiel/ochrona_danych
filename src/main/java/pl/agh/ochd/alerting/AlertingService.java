package pl.agh.ochd.alerting;


import pl.agh.ochd.domain.LogSample;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class AlertingService {

    private final String HOST;
    private final String FROM;
    private final String SUBJECT = "[ALERTS] Host name: ";
    private final String BODY = "Pattern with name: %s matches: \n\n";

    private Address[] emails;
    private Properties properties;
    private Session session;

    public AlertingService(String emailHost, String emailAddress, Collection<String> recipients) {

        HOST = emailHost;
        FROM = emailAddress;
        properties = System.getProperties();
        properties.setProperty("mail.smtp.host", HOST);
        session = Session.getDefaultInstance(properties);
        prepareRecipients(recipients);
    }

    private void prepareRecipients(List<String> recipients) {

        emails = new Address[recipients.size()];
        int i = 0;
        while (i < recipients.size()) {
            try {
                emails[i] = new InternetAddress(recipients.get(i));
            } catch (AddressException e) {
                // should not happen
                e.printStackTrace();
                throw new IllegalStateException("Can not parse email address: " + recipients.get(i));
            }
            i++;
        }
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

        MimeMessage message = prepareMessage(hostName, prepareMessageBody(patternName, alerts));

        try {
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // TODO
        }
    }

}
