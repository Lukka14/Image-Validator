package com.example.springboottest.controllers;

import com.example.springboottest.services.WebStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class MyRestController {
//    @RequestMapping(value = "api/checkPage")
//    public Map<?,?> checkPage(String webPageUrl){
//        return new WebStatus().getValidatedUrlMap(webPageUrl);
//    }
    @GetMapping(value = "api/getImageSrcFromPage")
    public Set<String> getImageSrcValuesFrom(String webPageUrl){
        return new WebStatus().getImageSrcValues(webPageUrl);
    }
    @GetMapping(value = "api/getImageRequestStatus")
    public int getImageRequestStatus(String webPageUrl,boolean proxyEnabled){
        return new WebStatus().getImageRequestStatus(webPageUrl,proxyEnabled);
    }
}
