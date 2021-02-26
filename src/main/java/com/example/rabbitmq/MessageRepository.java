package com.example.rabbitmq;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Vector;

@Repository
public class MessageRepository {
    private List<String> messages = new Vector<>();

    public List<String> findAll() {
        return messages;
    }

    public void save(String message) {
        messages.add(message);
    }
}
