package com.versionedkv.store.kv.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.versionedkv.store.kv.repository.KeyValueEntity;
import com.versionedkv.store.kv.repository.KeyValueRepository;
import com.versionedkv.store.kv.service.api.KeyValueService;
import com.versionedkv.store.shared.api.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeyValueServiceImpl implements KeyValueService {

    private final KeyValueRepository repository;

    @Transactional
    @Override
    public String create(JsonNode body) {
        Iterator<Map.Entry<String, JsonNode>> fields = body.fields();
        if (!fields.hasNext()) {
            throw new IllegalArgumentException("Request body must contain at least one key-value pair");
        }
        Map.Entry<String, JsonNode> entry = fields.next();
        String key = entry.getKey();
        String value = entry.getValue().asText();

        KeyValueEntity entity = repository.findByKey(key)
                .orElse(new KeyValueEntity());
        entity.setKey(key);
        entity.setValue(value);
        repository.save(entity);

        return value;
    }

    @Transactional(readOnly = true)
    @Override
    public String getByKey(String key) {
        KeyValueEntity entity = repository.findByKey(key)
                .orElseThrow(() -> new NotFoundException("Key not found: " + key));
        return entity.getValue();
    }
}
