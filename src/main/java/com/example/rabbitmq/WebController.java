package com.example.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.jms.ConnectionFactory;
import javax.jms.ObjectMessage;
import java.io.*;
import java.util.UUID;

@Controller
public class WebController {
    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    MessageRepository messageRepository;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(Model viewModel) {
        prepareViewModel(viewModel);
        return "index";
    }

    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public String process(@RequestParam String message) throws IOException, ClassNotFoundException {
        Serializable model = ofsModelFromBinary(message);
        sendModel(model);
        return "redirect:/index";
    }

    private Serializable ofsModelFromBinary(String message) throws IOException, ClassNotFoundException {
        try (
                InputStream is = new ByteArrayInputStream(Base64.decodeBase64(message)); // TODO Make this code easier to understand!!
                ObjectInputStream in = new ObjectInputStream(is)
        ) {
            return (Serializable) in.readObject();
        }
    }

    private OfsModel ofsModelFromMessage(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(message, OfsModel.class);
    }

    private void sendModel(Serializable model) {
        JmsTemplate tpl = new JmsTemplate(connectionFactory);
        tpl.setReceiveTimeout(2000);
        tpl.send(JmsConfig.QUEUE_NAME, session -> {
            ObjectMessage objectMessage = session.createObjectMessage(model);
            objectMessage.setJMSCorrelationID(UUID.randomUUID().toString());
            return objectMessage;
        });
    }

    private void prepareViewModel(Model model) {
        model.addAttribute("receivedMessages", messageRepository.findAll());
    }
}
