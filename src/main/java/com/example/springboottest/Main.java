package com.example.springboottest;

import com.example.springboottest.api.GmailApi;
import com.example.springboottest.services.CSVFile;

import javax.mail.MessagingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static boolean proxyEnabled;

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/main/resources/config/config.properties"));
        String logFilePath = properties.getProperty("logFilePath");
        proxyEnabled = Boolean.parseBoolean(properties.getProperty("proxyEnabled"));

        long generalStartTime = System.currentTimeMillis();
        GmailApi gmailApi = new GmailApi("898543226384-5qtd3kngjii2706l2mq2a846i00kk53c.apps.googleusercontent.com",
                "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",
                "1//09r0-YKCjMuzyCgYIARAAGAkSNwF-L9IrSVjHp1IQkYf6LAbAmNLOpBMFPcvxUV6ySsfFQlkXNaAKwOmUp-JpKWOvLh56zJE5WLU");
        CSVFile csvData = gmailApi.getAttachmentData("188d9147be8a0252");
        List<String> pageUrlList = csvData.getAllPageUrlAsList();

        StringBuilder message = new StringBuilder();
        message.append("Web Pages: " + pageUrlList.size()+"\n");
        message.append("Proxy enabled: " + proxyEnabled+"\n");
        File file = new File(logFilePath);

        HttpRequestExample httpRequestExample = new HttpRequestExample();
        for (int i = 0; i < 15; i++) {
            long pageStartTime = System.currentTimeMillis();
            String pageUrl = "https://" + pageUrlList.get(i);
            Map<Integer, Integer> statusCodesAndTheirQuantityMap = new HashMap<>();
            Map<String, Integer> imageStatusMap = httpRequestExample.getImageStatusMap(pageUrl, proxyEnabled);

            message.append("#" + (i + 1) + " ; " + pageUrl + " ; ");
            AtomicInteger validPictureCount = new AtomicInteger(0);
            if (imageStatusMap == null) {
                message.append("404 ; - ; - ; ");
            } else {
                message.append("200 ; ");
                imageStatusMap.forEach((imageUrl, statusCode) -> {
                    if(statusCode==200){
                        validPictureCount.getAndIncrement();
                    }
                    if (statusCodesAndTheirQuantityMap.containsKey(statusCode)) {
                        int quantity = statusCodesAndTheirQuantityMap.get(statusCode);
                        statusCodesAndTheirQuantityMap.put(statusCode, quantity + 1);
                    } else {
                        statusCodesAndTheirQuantityMap.put(statusCode, 1);
                    }

                });
                statusCodesAndTheirQuantityMap.forEach((statusCode, quantity) -> message.append(imageStatusMap.size()+ " ; "+ validPictureCount.get()+" ; "));
            }
            long timeElapsedPerPage = (System.currentTimeMillis() - pageStartTime);
            message.append(timeElapsedPerPage+"\n");
            writeInFileAndOnConsole(message.toString(),file);
            message.setLength(0);
        }

        long generalTimeElapsed = (System.currentTimeMillis() - generalStartTime);
        writeInFileAndOnConsole("General time elapsed: " + generalTimeElapsed+"\n",file);
        csvData.readResultLog();
        String filePath = "src/main/java/com/example/springboottest/api/csv/new6.csv";
//        csvData.writeCSVFile(filePath);
        try {
            gmailApi.sendMessage(GmailApi.createEmailWithAttachment("singlebatumi@gmail.com", "me", "es aris testiii..", "vin iyo kata?!", csvData.getDataAsString()));
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeInFileAndOnConsole(String message, File file){
        try(FileWriter fileWriter = new FileWriter(file,true)) {
            System.out.print(message);
            fileWriter.append(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
