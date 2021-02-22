package com.example.rabbitmq;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class Listener {
    private CountDownLatch latch = new CountDownLatch(1);

    @JmsListener(destination = "demoqueue")
    public void receiveMessage(String msg) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("Received message is: " + msg);
        System.out.println("========================================");

        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}