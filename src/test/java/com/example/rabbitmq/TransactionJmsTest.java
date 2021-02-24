package com.example.rabbitmq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.jms.*;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
public class MessageSelectorJmsTest {
    static final String destinationName = "testqueue";
    static final CountDownLatch latch = new CountDownLatch(1);

    @Autowired
    ConnectionFactory connectionFactory;

    @Test
    public void test() throws Exception {
        String messageContent = UUID.randomUUID().toString();

        JmsTemplate tpl = new JmsTemplate(connectionFactory);
        tpl.send(destinationName, session -> {
            ObjectMessage message = session.createObjectMessage(new TestMessage(1L, "one"));
            message.setJMSCorrelationID(messageContent);
            message.setStringProperty("type", "test"); // property to be used by the selector
            return message;
        });
        tpl.send(destinationName, session -> { // message to be ignored
            ObjectMessage message = session.createObjectMessage(new TestMessage(2L, "two"));
            message.setJMSCorrelationID(messageContent);
            return message;
        });

        latch.await(2000, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(0, latch.getCount());
    }

    @TestConfiguration
    @EnableJms
    static class Config {
        @Bean
        public ConnectionFactory connectionFactory() {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false&broker.useShutdownHook=false");
            connectionFactory.setTrustAllPackages(true);
            return connectionFactory;
        }

        @Bean
        public DefaultMessageListenerContainer jmsContainer(ConnectionFactory connectionFactory, MessageListener messageListener) {
            DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.setDestinationName(destinationName);
            container.setMessageListener(messageListener);
            container.setMessageSelector("type='test'");
            return container;
        }

        @Bean
        public MessageListener messageListener() {
            return new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    System.out.println("******************* Received: " + msg);
                    latch.countDown();
                }
            };
        }

//        @Bean
//        public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
//            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//            factory.setConnectionFactory(connectionFactory);
//            return factory;
//        }
    }

    static class TestMessage implements Serializable {
        private Long id;
        private String name;

        public TestMessage(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}