package pl.agh.ochd.alerting;


import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

public class AlertingService {

    private final String HOST = "localhost";    // email client address
    private final String FROM = "admin@admin.com";

    private Address[] emails;
    private Properties properties;
    private Session session;

    public AlertingService(List<String> recipients) {

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
            }
            i++;
        }
    }

    private MimeMessage prepareMessage(String subject, String messageBody) {

        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(FROM));
            message.addRecipients(Message.RecipientType.TO, emails);
            message.setSubject(subject);
            message.setText(messageBody);
        } catch (MessagingException e) {}

        return message;
    }

    public void sendAlertNotification(String subject, String messageBody) {

        MimeMessage message = prepareMessage(subject, messageBody);

        try {
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // TODO ???
        }
    }

}
