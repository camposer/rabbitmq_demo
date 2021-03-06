package com.example.rabbitmq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.test.context.ActiveProfiles;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("simpleJmsTest")
public class SimpleJmsTest {
    static final String destinationName = "testqueue";

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

        SimpleJmsMessageListener.latch.await(5000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(0, SimpleJmsMessageListener.latch.getCount());
    }

    @TestConfiguration
    @EnableJms
    static class Config {
        @Bean
        public ConnectionFactory connectionFactory() {
            return new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false&broker.useShutdownHook=false");
        }

        @Bean
        public DefaultMessageListenerContainer jmsContainer(ConnectionFactory connectionFactory, MessageListener messageListener) {
            DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.setDestinationName(destinationName);
            container.setMessageListener(messageListener);
            return container;
        }

//        @Bean // You want to return your bean here!!!
//        public MessageListener messageListener() {
//            return new MessageListener() {
//                @Override
//                public void onMessage(Message msg) {
//                    System.out.println("******************* Received: " + msg);
//                    latch.countDown();
//                }
//            };
//        }

        @Bean
        public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            return factory;
        }
//
//        @Bean
//        public Object listener() {
//            return new Object() {
//                @JmsListener(destination = destinationName)
//                public void receiveMessage(String msg) {
//                    System.out.println("******************* Received: " + msg);
//                    latch.countDown();
//                }
//            };
//        }
    }
}
