package com.example.springboottest.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class WebStatus {
//    public static final String STATUS_CODE_OK = "Ok";
//    public static final String STATUS_CODE_INVALID = "invalid";
//    public static final String STATUS_CODE_DOWN = "down";

    private final int maxWaitTimeInMills = 10000;

    //todo es mushaobs !!
//    public Map<?, ?> getValidatedUrlMap(String url) {
//        List<String> invalidImageUrlList = new ArrayList<>();
//        Map<String, Integer> validUrlMap = new HashMap<>();
//        Set<String> imgSrcArray = getImageSrcValues(url);
//        int size = imgSrcArray.size();
//        int i = 1;
//        for (String src : imgSrcArray) {
//            if (!src.trim().endsWith(".gif")) {
//                validUrlMap.put(src, getImageRequestStatus(src, true));
//            }
//            System.out.println("src = " + src);
//        }
//        System.out.println("validUrlMap.size() = " + validUrlMap.size());
//        validUrlMap.forEach((key, value) -> System.out.println(key + " is valid: " + value));
//        return validUrlMap;
//    }

    public String checkPage(String pageUrl) {
        String pageStatus = PageStatus.Ok.toString();
        Set<String> pageImageUrlSet = getImageSrcValues(pageUrl);
        Map<String, String> pageImageUrlStatusMap = getImageStatusMap(pageImageUrlSet);
        if (pageImageUrlStatusMap.containsValue(PageStatus.Invalid.toString())){
            pageStatus = "invalid";
        } else if (pageImageUrlStatusMap.containsValue(PageStatus.Down.toString())) {
            pageStatus = "Down";
        }
        return pageStatus;
    }

    public static Map<String, String> getImageStatusMap(Set<String> imageUrlSet) {
        Map<String, String> statusMap = new HashMap<>();
        for (String imageUrl : imageUrlSet) {
            switch (WebStatus.getImageRequestStatus(imageUrl, true)) {
                case 200 -> statusMap.put(imageUrl, PageStatus.Ok.toString());
                case 403 -> statusMap.put(imageUrl, PageStatus.Invalid.toString());
                default -> statusMap.put(imageUrl, PageStatus.Down.toString());
            }
        }
        return statusMap;
    }

    public Map<String, Set<String>> getImageSrcValues(List<String> urlList) {
        Map<String, Set<String>> pageAndImgUrlMap = new HashMap<>();
        urlList.forEach(url -> pageAndImgUrlMap.put(url, getImageSrcValues(url)));
        return pageAndImgUrlMap;
    }

//    public Map<String, Set<String>> getImageSrcValues(List<String> urlList) {
//        return urlList.stream().collect(Collectors.toMap(url ->url,this::getImageSrcValues ,(a,b) -> a,HashMap::new));
//    }

    public Set<String> getImageSrcValues(String url) {
        Set<String> imgSrcArraySet = new HashSet<>();
        try {
            System.out.println("Working on it...");
            Document doc = Jsoup.connect(url).timeout(maxWaitTimeInMills).get();
            Elements imgElements = doc.select("img[src]");
            System.out.println("imgElements.size() = " + imgElements.size());
            for (int i = 0; i < imgElements.size(); i++) {
                Element img = imgElements.get(i);
                String src = img.attr("src");
                if (!src.trim().endsWith(".gif")) {
                    imgSrcArraySet.add(src);
                }
            }
            return null;
        } catch (SocketTimeoutException e) {
            System.out.println("Page not found, Website is down.");
            System.out.println("Web Page URL: " + url);
//            imgSrcArraySet.add("NULL");
        } finally {
            return imgSrcArraySet;
        }
    }

    public static int getImageRequestStatus(String imageUrl, boolean proxyEnabled) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection;
            if (proxyEnabled) {
                String proxyHost = "166.78.241.15";
                int proxyPort = 8080;
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
//            System.out.println(" ================ ");
//            System.out.println("imageUrl = " + imageUrl);
//            System.out.println("responseCode = " + responseCode);
//            System.out.println(" ================ ");
            return (responseCode);
        } catch (IOException e) {
            return -1;
        }
    }
}