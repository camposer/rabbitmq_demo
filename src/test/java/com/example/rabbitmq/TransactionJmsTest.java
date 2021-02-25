package com.example.rabbitmq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ErrorHandler;

import javax.jms.*;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
public class TransactionJmsTest {
    static final String destinationName = "testqueue";
    static final CountDownLatch latch = new CountDownLatch(1);

    static boolean failedOnFirstCall = false;

    @Autowired
    ConnectionFactory connectionFactory;

    @Test
    @Disabled
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
            return container;
        }

        @Bean
        public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
                ConnectionFactory connectionFactory,
                //DefaultJmsListenerContainerFactoryConfigurer configurer,
                PlatformTransactionManager transactionManager
        ) {
            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            //configurer.configure(factory, connectionFactory);
            factory.setTransactionManager(transactionManager);
            return factory;
        }

        @Bean
        public DefaultJmsListenerContainerFactoryConfigurer jmsListenerContainerFactoryConfigurer() {
            return new DefaultJmsListenerContainerFactoryConfigurer();
        }

        @Bean
        public PlatformTransactionManager transactionManager(ConnectionFactory connectionFactory) {
            JmsTransactionManager transactionManager = new JmsTransactionManager();
            transactionManager.setConnectionFactory(connectionFactory);
            return transactionManager;
        }


        @Bean
        public MessageListener messageListener() {
            return new MessageListener() {
                @Override
                @Transactional
                public void onMessage(Message msg) {
                    if (!failedOnFirstCall) {
                        failedOnFirstCall = true;
                        throw new JmsException("Error processing this message!!!") {
                        };
                    }

                    System.out.println("******************* Received: " + msg);
                    latch.countDown();
                }
            };
        }
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
