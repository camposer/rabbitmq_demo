package com.example.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.TextMessage;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class RabbitmqApplication implements CommandLineRunner {
	@Autowired
	ConnectionFactory connectionFactory;

	@Autowired
	Listener listener;

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(RabbitmqApplication.class, args).close();
	}

	@Override
	public void run(String... args) throws Exception {
		String messageContent = UUID.randomUUID().toString();

		JmsTemplate tpl = new JmsTemplate(connectionFactory);
		tpl.setReceiveTimeout(2000);
		tpl.send("demoqueue", session -> {
			TextMessage message = session.createTextMessage(messageContent);
			message.setJMSCorrelationID(messageContent);
			return message;
		});

		listener.getLatch().await(10000, TimeUnit.MILLISECONDS);
	}
}
