package com.example.springboottest.controllers;

import com.example.springboottest.api.GmailApi;
import com.example.springboottest.services.CSVFile;
import com.example.springboottest.services.WebStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.springboottest.api.GmailApi.createEmailWithAttachment;

@Controller
public class MyController {
    volatile String runImageUrl;
    volatile int runImageStatus;

    @RequestMapping("/")
    public String index() {
        WebStatus webStatus = new WebStatus();
        GmailApi gmailApi = new GmailApi("898543226384-5qtd3kngjii2706l2mq2a846i00kk53c.apps.googleusercontent.com",
                "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",
                "1//09r0-YKCjMuzyCgYIARAAGAkSNwF-L9IrSVjHp1IQkYf6LAbAmNLOpBMFPcvxUV6ySsfFQlkXNaAKwOmUp-JpKWOvLh56zJE5WLU");
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                runImageStatus = webStatus.getImageRequestStatus(runImageUrl, true);
//            }
//        };


        Map<String, String> pageStatusMap = new HashMap<>();
        CSVFile csvData = gmailApi.getAttachmentData("188d9147be8a0252");
        List<String> urls = csvData.getAllPageUrlAsList();
        pageStatusMap.put("shortedUrl", "status");
        for (String url : urls) {
                pageStatusMap.put(url, webStatus.checkPage(url));
        }
        pageStatusMap.forEach((k,v) -> System.out.println(k + " : " + v));
        csvData.appendStatus(pageStatusMap);
        try {
            csvData.writeCSVFile("src/main/java/com/example/springboottest/api/csv/new2.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MimeMessage mimeMessage;
        try {
            mimeMessage = createEmailWithAttachment("trendbatumi2@gmail.com", "me", "es aris testiii..", "vin iyo kata?!", new File("src/main/java/com/example/springboottest/api/csv/new2.csv"));
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        gmailApi.sendMessage(mimeMessage);
        return "index";
    }

//    @PostMapping("/submit")
//    public String submitForm(@RequestParam("url") String url) {
//        System.out.println("Entered name: " + url);
//        return "redirect:/";
//    }

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
        model.addAttribute("saxeli", name);
        return "greeting";
    }

}
