/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.myic.email.body.search;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.jsoup.Jsoup;
import org.simplejavamail.converter.EmailConverter;

/**
 *
 * @author jules
 */
public class Main {

    public static void main(String[] args) throws IOException, MessagingException {
        MimeMessage message = EmailConverter.emlToMimeMessage(new File("/tmp/CANCELLATION.eml"));
        String text = getTextFromMessage(message);

        System.out.println(text);
        System.out.println("---------------------------------------------------------------------------");

        Pattern pattern = Pattern.compile("(ID NUMBER)\\s*(:)\\s*\\d{13} | \\d{13}");
        Matcher matcher = pattern.matcher(text);

        List<String> ms = new LinkedList<>();
        if (matcher.find()) {
            String found = matcher.group();
            System.out.println("Found word: " + found + ", adding to list...");
            found = found.replaceAll("\\s*", "");
            ms.add(found);
        }
        int count = 1;
        for (String m : ms) {

            System.out.println(count + " <---> " + m);
            count += 1;
        }
    }

    private static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private static String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break;
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result += getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }
}
