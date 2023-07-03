package com.example.springboottest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ImageUrlExtractor {
        public static void main(String[] args) {
            Set<String> imageUrlSet = getImagesFromPage("https://46227-selenium-ln.spw4u.com/");
            System.out.println("imageUrlSet.size() = " + imageUrlSet.size());
            int i=0;
            long startTime = System.currentTimeMillis();
            for (String imageUrl : imageUrlSet){
                System.out.println("imageUrl = " + imageUrl+" "+" #"+(++i));
            }
            System.out.println("Time elapsed: "+(System.currentTimeMillis()-startTime));
            startTime = System.currentTimeMillis();
            i=0;
            for (String imageUrl : imageUrlSet){
                System.out.println("imageUrl = " + imageUrl+" "+getImageRequestStatus(imageUrl,true)+" #"+(++i));
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

    public static int getImageRequestStatus(String imageUrl, boolean proxyEnabled) {
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
            connection.setRequestMethod("HEAD");

            // Get the response code
            int responseCode = connection.getResponseCode();

            return responseCode;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1; // Return -1 in case of an exception
    }

}

