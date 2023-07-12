package com.example.springboottest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class ImageUrlExtractor {
        public static void main(String[] args){
            Set<String> imageUrlSet = getImagesFromPage("https://37784-alder-court.spw4u.com/");
//            Set<String> imageUrlSet = new HashSet<>();
            System.out.println("imageUrlSet.size() = " + imageUrlSet.size());
            int i=0;
            long startTime = System.currentTimeMillis();
//            imageUrlSet.add("https://media.crmls.org/medias/e59effc7-d434-4c9b-9974-e57dcd6ce8f7.jpg?preset=X-Large");
            for (String imageUrl : imageUrlSet){
                System.out.println("imageUrl = " + imageUrl+" "+" #"+(++i));
            }
            System.out.println("Time elapsed: "+(System.currentTimeMillis()-startTime));
            startTime = System.currentTimeMillis();
            i=0;
            for (String imageUrl : imageUrlSet){
                System.out.println("imageUrl = " + imageUrl+" "+getImageRequestStatus(imageUrl,false)+" #"+(++i));
            }
            System.out.println("Time elapsed: "+(System.currentTimeMillis()-startTime));
        }

        public static Set<String> getImagesFromPage(String pageURL){
            Set<String> imageUrlSet = new HashSet<>();
            try {
                Document doc = Jsoup.connect(pageURL).get();
                Elements images = doc.select("img");

                for (Element image : images) {
                    String imageUrl = image.absUrl("src");
                    if(!imageUrl.endsWith(".gif") && imageUrl.trim().length() > 0){
                        imageUrlSet.add(imageUrl);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return imageUrlSet;
        }

    public static int getImageRequestStatus(String imageUrl, boolean proxyEnabled)   {
        try {
            URL url = new URL(imageUrl);

            // Create a connection to the URL
            HttpURLConnection connection;

            String proxyHost = "166.78.241.15";
            int proxyPort = 8080;

            if (proxyEnabled) {
                // If proxy is enabled, create a proxy object with the appropriate host and port
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            // Set the request method to HEAD
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if(!proxyEnabled && (responseCode == 403 || responseCode==404)) {
                return getImageRequestStatus(imageUrl,true);
            }

            // Get the response code
            return responseCode;
        }catch (IOException e){
//            throw new RuntimeException(e);
            return 502;
        }
    }

//    public static boolean imageExists(String imageUrl){
//        try {
//            return ImageIO.read(new URL(imageUrl))!=null;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    // safe to delete
    public static int imageExists(String imageUrl) {
        String proxyHost = "166.78.241.15";
        int proxyPort = 8080;
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);

            int responseCode = connection.getResponseCode();
            return responseCode;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

