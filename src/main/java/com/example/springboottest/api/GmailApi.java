package com.example.springboottest.api;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class GmailApi {
    public static final String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    private static final String URL_COLUMN_NAME = "shortedUrl";
    private final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private final String GRANT_TYPE = "refresh_token";
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
    private byte[] getAttachmentBytes(Gmail gmail, String messageId, String attachmentId)
            throws IOException {
        MessagePartBody attachmentPart = gmail.users().messages().attachments()
                .get(USER, messageId, attachmentId).execute();
        return com.google.api.client.util.Base64.decodeBase64(attachmentPart.getData());
    }
    public List<List<String>> getMailBody(String messageId) throws IOException {
        List<List<String>> urlList = new ArrayList<>();
        Message fullMessage = getMessageById(messageId);

        List<MessagePart> parts = fullMessage.getPayload().getParts();
        if (parts != null) {
            for (MessagePart part : parts) {
                String filename = part.getFilename();
                String attachmentId = part.getBody().getAttachmentId();
                if (filename != null && attachmentId != null) {
                    byte[] attachmentBytes = getAttachmentBytes(gmailService, messageId, attachmentId);
                    urlList = processCSVFile(attachmentBytes);
                }
            }
        }
        return urlList;
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
            response = sendPostRequest(TOKEN_URL, requestBody);
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
    private String sendPostRequest(String url, String requestBody) throws Exception {
        return Unirest.post(url)
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
    private List<List<String>> processCSVFile(byte[] attachmentBytes) throws IOException {
//        List<String> urlList = new ArrayList<>();
        List<List<String>> urls = new ArrayList<>();
        InputStreamReader inputStreamReader = new InputStreamReader(new ByteArrayInputStream(attachmentBytes));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();
        line = bufferedReader.readLine();
        List<String> data = new ArrayList<>(List.of(line.split(",")));
        urls.add(data);
//        int urlIndex = getUrlIndex(line);
        int i = 0; //todo wasashleli
        while ((line = bufferedReader.readLine()) != null && i < 10) {
            data = new ArrayList<>(List.of(line.split("\",\"")));
            urls.add(data);
//            String[] data = line.split("\",\"");
//            urlList.add(data[urlIndex]);
            i++; //todo wasashleli
        }

//        writeCSVFile(urls);
        bufferedReader.close();
        inputStreamReader.close();

        return urls;
    }

    public List<String> getPageUrl () throws IOException {
        List<String> pageUrlList = new ArrayList<>();
        List<List<String>> csvData = getMailBody("188d9147be8a0252");
        List<String> rowData = csvData.get(0);
        int pageUrlIndex = getUrlIndex(rowData);
        for (int i = 1; i < csvData.size(); i++) {
            pageUrlList.add(csvData.get(i).get(pageUrlIndex));
        }
        return pageUrlList;
    }

    public void writeCSVFile(List<List<String>> urls, List<String> status) throws IOException {
        File file = new File("src/main/java/com/example/springboottest/api/csv/new.csv");
        FileWriter csvWriter = new FileWriter(file);
        System.out.println("urls.size() = "+ urls.size());
        System.out.println("status.size() = " + status.size());
        int index = 0;
        csvWriter.append("sep=,\n");
        csvWriter.append(String.join(",", urls.get(index)));
        csvWriter.append(",").append(status.get(index));
        csvWriter.append("\n");
        index++;
        for (; index < urls.size(); index++) {
            csvWriter.append(String.join("\",\"", urls.get(index)));
            csvWriter.append(",\"").append(status.get(index)).append("\"");
            csvWriter.append("\n");
        }
        csvWriter.close();
    }

    public int getUrlIndex(List<String> data) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).equals(URL_COLUMN_NAME)) return i;
        }
        return -1;
    }
}