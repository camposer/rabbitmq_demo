package com.example.rabbitmq;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Vector;

@Repository
public class OfsModelRepository {
    private List<OfsModel> models = new Vector<>();

    public List<OfsModel> findAll() {
        return models;
    }

    public void save(OfsModel model) {
        models.add(model);
    }
}
