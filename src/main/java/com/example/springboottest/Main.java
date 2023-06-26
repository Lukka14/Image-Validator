package com.example.springboottest;

public class Main {
    volatile int data = 5;

    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            final String threadName = "Thread 1";

            @Override
            public void run() {

                System.out.println("Runnable thread: " + threadName);
            }
        };

        Thread thread = new Thread(runnable);

        thread.start();
        for (int i = 0; i < 10; i++) {
            System.out.println("Main #" + i);
        }

    }


}
