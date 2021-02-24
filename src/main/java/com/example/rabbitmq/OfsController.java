package com.example.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.jms.ConnectionFactory;
import javax.jms.ObjectMessage;
import java.util.UUID;

@Controller
public class OfsController {
    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    OfsModelRepository ofsModelRepository;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(Model viewModel) {
        prepareViewModel(viewModel);
        return "index";
    }

    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public String process(@RequestParam String message, Model viewModel) throws JsonProcessingException {
        OfsModel ofsModel = ofsModelFromMessage(message);
        sendModel(ofsModel);
        prepareViewModel(viewModel);
        return "redirect:/index";
    }

    private OfsModel ofsModelFromMessage(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(message, OfsModel.class);
    }

    private void sendModel(OfsModel model) {
        JmsTemplate tpl = new JmsTemplate(connectionFactory);
        tpl.setReceiveTimeout(2000);
        tpl.send(JmsConfig.QUEUE_NAME, session -> {
            ObjectMessage objectMessage = session.createObjectMessage(model);
            objectMessage.setJMSCorrelationID(UUID.randomUUID().toString());
            return objectMessage;
        });
    }

    private void prepareViewModel(Model model) {
        model.addAttribute("receivedModels", ofsModelRepository.findAll());
    }
}
