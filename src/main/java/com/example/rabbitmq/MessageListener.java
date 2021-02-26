package com.example.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;

@Component
public class MessageListener {
    @Autowired
    MessageRepository messageRepository;

    @JmsListener(destination = JmsConfig.QUEUE_NAME)
    public void receiveMessage(Message message) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("Received model: " + message);
        System.out.println("========================================");
        messageRepository.save(message.toString());
    }
}