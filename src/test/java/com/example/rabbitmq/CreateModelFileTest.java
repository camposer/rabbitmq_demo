package com.example.rabbitmq;

import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateModelFileTest {
    // We need to execute something similar in the TransformationService
    // using the Event that we create in the test right now
    @Test
    public void test() throws IOException {
        OfsModel model = new OfsModel();
        model.setName("one");
        model.setType("type");

        String filename = "target/model.txt";

        // Serialization
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        // Method for serialization of object
        out.writeObject(model);

        byte[] encodedObject = Base64.encodeBase64(baos.toByteArray());
        Files.write(Paths.get(filename), encodedObject);

        out.close();
    }
}
