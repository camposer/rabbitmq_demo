package com.example.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class OfsModelListener {
    @Autowired
    OfsModelRepository ofsModelRepository;

    @JmsListener(destination = JmsConfig.QUEUE_NAME)
    public void receiveMessage(OfsModel ofsModel) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("Received model: " + ofsModel);
        System.out.println("========================================");
        ofsModelRepository.save(ofsModel);
    }
}