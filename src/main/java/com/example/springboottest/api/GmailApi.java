package com.example.springboottest.api;

import com.example.springboottest.services.CSVFile;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GmailApi {
    public static final String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final String GRANT_TYPE = "refresh_token";
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final String USER = "me";
    private final JSONObject clientCredentials = new JSONObject();
    private Gmail gmailService;

    public GmailApi(String clientId, String clientSecret, String refreshToken) {
        setCredentials(clientId, clientSecret, refreshToken);
        buildGmailService();
    }

    private void setCredentials(String clientId, String clientSecret, String refreshToken) {
        clientCredentials.put("client_id", clientId);
        clientCredentials.put("client_secret", clientSecret);
        clientCredentials.put("refresh_token", refreshToken);
    }

    private void buildGmailService() {
        try {
            Credential authorize = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(clientCredentials.get("client_id").toString(), clientCredentials.get("client_secret").toString())
                    .build()
                    .setAccessToken(getAccessToken(clientCredentials.get("client_id").toString(), clientCredentials.get("client_secret").toString()))
                    .setRefreshToken(clientCredentials.get("refresh_token").toString());

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            gmailService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Error, could not authorize");
        }
    }

    private byte[] getAttachmentBytes(String messageId, String attachmentId)
            throws IOException {
        MessagePartBody attachmentPart = gmailService.users().messages().attachments()
                .get(USER, messageId, attachmentId).execute();
        return com.google.api.client.util.Base64.decodeBase64(attachmentPart.getData());
    }

    public CSVFile getAttachmentData(String messageId) {
        CSVFile csvFile = null;
        try {
            Message fullMessage = getMessageById(messageId);

            List<MessagePart> parts = fullMessage.getPayload().getParts();
            if (parts != null) {
                for (MessagePart part : parts) {
                    String filename = part.getFilename();
                    String attachmentId = part.getBody().getAttachmentId();
                    if (filename != null && attachmentId != null) {
                        byte[] attachmentBytes = getAttachmentBytes(messageId, attachmentId);
                        csvFile = processCSVFile(attachmentBytes);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return csvFile;
    }

    private Message getMessageById(String messageId) throws IOException {
        return gmailService.users().messages().get(USER, messageId).execute();
    }

    private String getAccessToken(String clientId, String clientSecret) {

        // Construct the POST request to exchange the refresh token for access token
        String requestBody = createRequestBody(clientId, clientSecret);

        // Send the POST request and parse the response to extract the access token
        String response = null;
        try {
            response = sendPostRequest(requestBody);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return extractAccessToken(response);
    }

    private String createRequestBody(String clientId, String clientSecret) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("client_id", clientId);
        jsonObject.put("client_secret", clientSecret);
        jsonObject.put("refresh_token", clientCredentials.get("refresh_token"));
        jsonObject.put("grant_type", GRANT_TYPE);
        return jsonObject.toString();
    }

    private String sendPostRequest(String requestBody) throws Exception {
        return Unirest.post(TOKEN_URL)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .asString().getBody(); // Return the response body as a string
    }

    private String extractAccessToken(String response) {
        int startOfAccess_token = response.lastIndexOf("\"access_token\"") + 17;
        String subString = response.substring(startOfAccess_token);
        int endOfAccess_token = subString.indexOf("\"");
        return subString.substring(0, endOfAccess_token); // Return the extracted access token
    }

    private CSVFile processCSVFile(byte[] attachmentBytes) throws IOException {

        InputStreamReader inputStreamReader = new InputStreamReader(new ByteArrayInputStream(attachmentBytes));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        List<String> csvFileRow = new ArrayList<>();
        String line;
//        int i = 0;
        while ((line = bufferedReader.readLine()) != null) {
            csvFileRow.add(line);
//            i++;
        }
        CSVFile csvFile = new CSVFile(csvFileRow);

        bufferedReader.close();
        inputStreamReader.close();

        return csvFile;
    }

    public void sendMessage(MimeMessage email) {
        Message message = createMessageWithEmail(email);
        try {
            message = gmailService.users().messages().send(USER, message).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Message Id:" + message.getId());
        try {
            System.out.println(message.toPrettyString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        MimeMessage email = new MimeMessage(getMimeMessage(from, to, subject));
        email.setText(bodyText);
        return email;
    }

    private static MimeMessage getMimeMessage(String from, String to, String subject) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        return email;
    }

    public static MimeMessage createEmailWithAttachment(String to, String from, String subject, String bodyText, String dataAsString) throws MessagingException {
        MimeMessage email = getMimeMessage(from, to, subject);
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(bodyText, "text/csv");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        mimeBodyPart = new MimeBodyPart();
        String attachmentName = "file.csv";
        try {
            mimeBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(dataAsString, "text/csv")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mimeBodyPart.setFileName(attachmentName);

        multipart.addBodyPart(mimeBodyPart);
        email.setContent(multipart, "text/csv");
        return email;
    }

    private Message createMessageWithEmail(MimeMessage email) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            email.writeTo(byteArrayOutputStream);
        } catch (IOException | MessagingException e) {
            throw new RuntimeException(e);
        }
        String encodedEmail = Base64.encodeBase64String(byteArrayOutputStream.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public List<Message> getListSearchedMessages(String search) throws IOException {
        List<Message> messageStubs = gmailService.users().messages().list(USER).setQ(search).execute().getMessages();
        return messageStubs == null ? new ArrayList<>() : messageStubs;
    }
}