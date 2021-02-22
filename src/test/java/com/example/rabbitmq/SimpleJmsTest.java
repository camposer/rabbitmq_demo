package com.example.rabbitmq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.jms.ConnectionFactory;
import javax.jms.TextMessage;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
public class SimpleJmsTest {
    static final String destinationName = "testqueue";
    static final CountDownLatch latch = new CountDownLatch(1);

    @Autowired
    ConnectionFactory connectionFactory;

    @Test
    public void test() throws Exception {
        String messageContent = UUID.randomUUID().toString();

        JmsTemplate tpl = new JmsTemplate(connectionFactory);
        tpl.send(destinationName, session -> {
            TextMessage message = session.createTextMessage(messageContent);
            message.setJMSCorrelationID(messageContent);
            return message;
        });

        latch.await(2000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(0, latch.getCount());
    }

    @Configuration
    @EnableJms
    static class Config {
        @Bean
        public ConnectionFactory connectionFactory() {
            return new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false&broker.useShutdownHook=false");

        }

        @Bean
        public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            return factory;
        }

        @Bean
        public Object listener() {
            return new Object() {
                @JmsListener(destination = destinationName)
                public void receiveMessage(String msg) {
                    System.out.println("******************* Received: " + msg);
                    latch.countDown();
                }
            };
        }
    }
}
