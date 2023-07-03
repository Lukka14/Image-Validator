package com.example.springboottest;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequestExample {

    private Map<String,Integer> imageStatusMap;

    public static void main(String[] args) {
        //String imageUrl = "https://bsu.edu.ge";
//        String websiteUrl = "https://5175-s-canal-circle.spw4u.com/";
        String websiteUrl = "https://222-n-rock-river-dr.spw4u.com/";
        long startTime = System.currentTimeMillis();
//        int statusCode = checkImageExists(websiteUrl);
//        System.out.println("Image response code: " + statusCode);
        HttpRequestExample httpRequestExample = new HttpRequestExample();
        Map<String,Integer> imageStatusMap = httpRequestExample.getImageStatusMap(websiteUrl,true);
        System.out.println("imageStatusMap.size() = " + imageStatusMap.size());
        imageStatusMap.forEach((key,value) -> System.out.printf("%s : %d\n",key,value));
        System.out.println("Time elapsed: "+(System.currentTimeMillis()-startTime));
    }

    public Map<String,Integer> getImageStatusMap(String websiteUrl,boolean proxyEnabled){
        imageStatusMap = new HashMap<>();
        if(!checkImageExists(websiteUrl,proxyEnabled)){
            return null;
        }
        return imageStatusMap;
    }

    private boolean checkImageExists(String websiteUrl,boolean proxyEnabled) {
        WebClient webClient = WebClient.builder().build();
        long startTime = System.currentTimeMillis();

        webClient.get()
                .uri(websiteUrl)
                .exchange()
                .doOnSuccess(response -> {
//                    System.out.println(websiteUrl+" : "+response.statusCode().value()+" : "+(System.currentTimeMillis()-startTime)/1000);
                })
                .block();


        Mono<String> htmlContentMono = webClient.get()
                .uri(websiteUrl)
                .accept(MediaType.TEXT_HTML)
                .retrieve()
                .bodyToMono(String.class);

        try {

            htmlContentMono.flatMapMany(htmlContent -> {
                Flux<String> imageUrls = extractImageUrlsFromHtml(htmlContent);
                imageUrls = imageUrls.filter(imageUrl -> {
                    if (imageUrl.endsWith(".gif")) {
                        return false;
                    }
                    if (imageUrl.endsWith("soon.jpg")) {
                        imageStatusMap.put(imageUrl, 403);
                        return false;
                    }
                    return true;
                });
                return imageUrls.flatMap(imageUrl -> checkImageLoading(webClient, imageUrl, proxyEnabled));
            }).blockLast();
        }catch (WebClientResponseException e){
            if(e.getStatusCode().value()==404){
                return false;
            }
        }

//        System.out.println(" : "+((System.currentTimeMillis() - startTime) / 1000));
        return true;
    }
    private static Flux<String> extractImageUrlsFromHtml(String htmlContent) {
        String regex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlContent);

        return Flux.fromStream(matcher.results()
                .map(result -> result.group(1)));
    }
    private Mono<Void> checkImageLoading(WebClient webClient, String imageUrl,boolean proxyEnabled) {
        long startTime = System.currentTimeMillis();
        return webClient.get()
                .uri(imageUrl)
                .exchange()
                .doOnSuccess(response -> {
                    if (response.statusCode().value()==302){
                        String newImageURL = response.headers().header("Location").get(0);
                        int imageStatusCode = ImageUrlExtractor.getImageRequestStatus(newImageURL,proxyEnabled);
//                        System.out.println("302 Image status: "+imageStatusCode);
                        imageStatusMap.put(newImageURL,imageStatusCode);
//                        System.out.println("Time elapsed: "+(System.currentTimeMillis()-startTime)/1000);
                    } else{
                        imageStatusMap.put(imageUrl,response.statusCode().value());
//                        System.out.println(imageUrl+" : "+response.statusCode().value()+" : "+(System.currentTimeMillis()-startTime)/1000);
                    }
                })
                .then();
    }
}

