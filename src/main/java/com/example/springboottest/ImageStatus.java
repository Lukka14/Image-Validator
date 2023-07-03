package com.example.springboottest;

public class ImageStatus implements Comparable<ImageStatus>{
    private String imageUrl;
    private int imageResponseCode;

    private long responseTime;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getImageResponseCode() {
        return imageResponseCode;
    }

    public void setImageResponseCode(int imageResponseCode) {
        this.imageResponseCode = imageResponseCode;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    @Override
    public int compareTo(ImageStatus other) {
        // Implement the natural ordering based on your requirements
        // For example, compare based on response time
        return Long.compare(this.getResponseTime(), other.getResponseTime());
    }
}

