package com.example.springboottest.controllers;

import com.example.springboottest.HttpRequestExample;
import com.example.springboottest.ImageStatus;
import com.example.springboottest.api.GmailApi;
import com.example.springboottest.services.CSVFile;
import com.example.springboottest.services.WebStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class MyController {
    volatile Set<ImageStatus> imageStatusListWithMulti = new HashSet<>();
    volatile Set<ImageStatus> imageStatusListWithoutMulti = new HashSet<>();

    private static final boolean proxyEnabled = true;

    class MyRunnable implements Runnable {
        private final CountDownLatch latch;
        private final WebStatus webStatus = new WebStatus();

        String imageUrl;

        public MyRunnable(CountDownLatch latch, String imageUrl) {
            this.imageUrl = imageUrl;
            this.latch = latch;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();

            int responseCode = webStatus.getImageRequestStatus(imageUrl, proxyEnabled);
            ImageStatus imageStatus = new ImageStatus();
            imageStatus.setImageUrl(imageUrl);
            imageStatus.setImageResponseCode(responseCode);

            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            imageStatus.setResponseTime(elapsedTime);
            imageStatusListWithMulti.add(imageStatus);
            latch.countDown();
        }
    }

    public void start(String imageUrl) {
        final WebStatus webStatus = new WebStatus();
        long startTime = System.currentTimeMillis();

        int responseCode = webStatus.getImageRequestStatus(imageUrl, proxyEnabled);
        ImageStatus imageStatus = new ImageStatus();
        imageStatus.setImageUrl(imageUrl);
        imageStatus.setImageResponseCode(responseCode);

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        imageStatus.setResponseTime(elapsedTime);
        imageStatusListWithoutMulti.add(imageStatus);
    }


    @RequestMapping("/")
    public String index() {
        return "index";
    }

    public static void main(String[] args) {
        long generalStartTime = System.currentTimeMillis();
        GmailApi gmailApi = new GmailApi("898543226384-5qtd3kngjii2706l2mq2a846i00kk53c.apps.googleusercontent.com",
                "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",
                "1//09r0-YKCjMuzyCgYIARAAGAkSNwF-L9IrSVjHp1IQkYf6LAbAmNLOpBMFPcvxUV6ySsfFQlkXNaAKwOmUp-JpKWOvLh56zJE5WLU");
        CSVFile csvData = gmailApi.getAttachmentData("188d9147be8a0252");
        List<String> pageUrlList = csvData.getAllPageUrlAsList();

        StringBuilder message = new StringBuilder();
        message.append("Web Pages: " + pageUrlList.size()+"\n");
        File file = new File("src/main/resources/log/resultLog.txt");

        HttpRequestExample httpRequestExample = new HttpRequestExample();
        for (int i = 0; i < pageUrlList.size(); i++) {
            long pageStartTime = System.currentTimeMillis();
            String pageUrl = "https://" + pageUrlList.get(i);
            Map<Integer, Integer> statusCodesAndTheirQuantityMap = new HashMap<>();
            Map<String, Integer> imageStatusMap = httpRequestExample.getImageStatusMap(pageUrl, proxyEnabled);

            message.append("#" + (i + 1) + "  " + pageUrl + " ; ");
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
        writeInFileAndOnConsole("General time elapsed: " + generalTimeElapsed,file);
    }

    public static void writeInFileAndOnConsole(String message, File file){
        try(FileWriter fileWriter = new FileWriter(file,true)) {
            System.out.print(message);
            fileWriter.append(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("/test")
    public String test() {
        WebStatus webStatus = new WebStatus();
        GmailApi gmailApi = new GmailApi("898543226384-5qtd3kngjii2706l2mq2a846i00kk53c.apps.googleusercontent.com",
                "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",
                "1//09r0-YKCjMuzyCgYIARAAGAkSNwF-L9IrSVjHp1IQkYf6LAbAmNLOpBMFPcvxUV6ySsfFQlkXNaAKwOmUp-JpKWOvLh56zJE5WLU");
        CSVFile csvData = gmailApi.getAttachmentData("188d9147be8a0252");
        List<String> pageUrlList = csvData.getAllPageUrlAsList();
        int testElementCount = Math.min(pageUrlList.size(), 5);
        System.out.println("pageUrlList.size() = " + pageUrlList.size());
        if (pageUrlList.size() > 0) {
            return "index";
        }
        System.out.println("testElementCount = " + testElementCount);
        CountDownLatch latch = new CountDownLatch(testElementCount);

        Thread tread;
        for (int i = 0; i < testElementCount; i++) {
            String pageUrl = pageUrlList.get(i);
            Set<String> imageUrlSet = webStatus.getImageSrcValues(pageUrl);
            System.out.println("Page url:" + pageUrl + "      #" + imageUrlSet.size());

            for (var imageUrl : imageUrlSet) {
                start(imageUrl);
            }

            for (var imageUrl : imageUrlSet) {
                Runnable runnable = new MyRunnable(latch, imageUrl);
                tread = new Thread(runnable);
                tread.start();
                tread.suspend();
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long elapsedTimeWithoutMultiThreading = 0;
        for (var imageStatus : imageStatusListWithoutMulti) {
            elapsedTimeWithoutMultiThreading += imageStatus.getResponseTime();
        }
        System.out.println("elapsed Time without multiThreading: " + elapsedTimeWithoutMultiThreading / 1000 + " seconds");

        long elapsedTimeWithMultiThreading = 0;
        for (var imageStatus : imageStatusListWithMulti) {
            elapsedTimeWithMultiThreading += imageStatus.getResponseTime();
        }
        System.out.println("elapsed Time with multiThreading: " + elapsedTimeWithMultiThreading / 1000 + " seconds");
        System.out.println("==================================");
        System.out.println("imageStatusListWithMulti.size() = " + imageStatusListWithMulti.size());
        System.out.println("imageStatusListWithoutMulti.size() = " + imageStatusListWithoutMulti.size());

        return "index";
    }

}