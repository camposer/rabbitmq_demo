package com.example.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

@Component
public class MessageListener {
    @Autowired
    MessageRepository messageRepository;

    @JmsListener(destination = JmsConfig.QUEUE_NAME)
    public void receiveMessage(Message message) throws JMSException {
        String messageString = ((ObjectMessage)message).getObject().toString();
        System.out.println();
        System.out.println("========================================");
        System.out.println("Received model: " + messageString);
        System.out.println("========================================");
        messageRepository.save(messageString);
    }
}