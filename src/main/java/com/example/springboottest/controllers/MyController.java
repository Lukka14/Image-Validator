package com.example.springboottest.controllers;

import com.example.springboottest.api.GmailApi;
import com.example.springboottest.services.WebStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

@Controller
public class MyController {
    volatile String runImageUrl;
    volatile int runIMageStatus;

    @RequestMapping("/")
    public String index() {
        WebStatus webStatus = new WebStatus();
        GmailApi gmailApi = new GmailApi("898543226384-5qtd3kngjii2706l2mq2a846i00kk53c.apps.googleusercontent.com",
                "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",
                "1//09r0-YKCjMuzyCgYIARAAGAkSNwF-L9IrSVjHp1IQkYf6LAbAmNLOpBMFPcvxUV6ySsfFQlkXNaAKwOmUp-JpKWOvLh56zJE5WLU");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                runIMageStatus = webStatus.getImageRequestStatus(runImageUrl, true);
            }
        };
        try {
            List<List<String>> urls = gmailApi.getMailBody("188d9147be8a0252");
            int urlIndex = gmailApi.getUrlIndex(urls.get(0));
            List<String> status = new ArrayList<>();
            status.add("status");
            for (List<String> url : urls) {
                System.out.println(url);
            }
            for (int i = 0; i < urls.size(); i++) {
                long begin = System.currentTimeMillis();
                Set<String> imageSrc = webStatus.getImageSrcValues(urls.get(i).get(urlIndex));
                int count = 0;
                Thread tread;
                for (String imageSrcValue : imageSrc) {
                    long imagBegin = System.currentTimeMillis();
                    System.out.println(imageSrcValue);
                    runImageUrl = imageSrcValue;
                    tread = new Thread(runnable);
                    tread.start();
                    if (runIMageStatus == 200) {
                        System.out.println("200");
                        count++;
                    }

                    long imageEnd = System.currentTimeMillis();
                    System.out.println("suratis droi = " + (imageEnd - imagBegin) / 1000.0);
                }
                if (count == imageSrc.size()) {
                    System.out.println("Ok");
                    status.add("Ok");
                } else {
                    System.out.println("Bad");
                    status.add("Bad");
                }
                long end = System.currentTimeMillis();
                System.out.println("sruli droi = " + (end - begin) / 1000.0);
            }

            gmailApi.writeCSVFile(urls,status);

//            urls.stream().map((str) -> {
//                Set<String> imageSrc = webStatus.getImageSrcValues(str);
//                int count = 0;
//                for (String imageSrcValue : imageSrc) {
//                    if(webStatus.getImageRequestStatus(imageSrcValue,true) == 200){
//                        count++;
//                    }
//                }
//                if (count == imageSrc.size()) {
//                    return "ok";
//                }else {
//                    return "down";
//                }
//            }).collect(Collectors.toList());
//            urls.forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
