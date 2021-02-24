package com.example.rabbitmq;

import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.CountDownLatch;

@Component
public class SimpleJmsMessageListener implements MessageListener {
    static final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void onMessage(Message msg) {
        System.out.println("******************* Received: " + msg);
        latch.countDown();
    }
}
