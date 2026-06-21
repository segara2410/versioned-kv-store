package com.versionedkv.store.kv.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionedkv.store.kv.repository.entity.KeyValueEntity;
import com.versionedkv.store.kv.repository.entity.KeyValueVersionEntity;
import com.versionedkv.store.kv.repository.KeyValueRepository;
import com.versionedkv.store.kv.repository.KeyValueVersionRepository;
import com.versionedkv.store.kv.service.KeyValueService;
import com.versionedkv.store.shared.api.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeyValueServiceImpl implements KeyValueService {

    private final KeyValueRepository repository;
    private final KeyValueVersionRepository versionRepository;

    @Transactional
    @Override
    public void create(JsonNode body) {
        Iterator<Map.Entry<String, JsonNode>> fields = body.fields();
        if (!fields.hasNext()) {
            throw new IllegalArgumentException("Request body must contain at least one key-value pair");
        }

        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            keys.add(entry.getKey());
            values.add(entry.getValue().asText());
        }

        repository.batchUpsert(keys, values);

        List<KeyValueVersionEntity> versions = repository.findByKeyIn(keys).stream()
                .map(e -> new KeyValueVersionEntity(e.getKey(), values.get(keys.indexOf(e.getKey())), e.getVersion()))
                .toList();
        versionRepository.saveAll(versions);
    }

    @Transactional(readOnly = true)
    @Override
    public String getByKey(String key) {
        KeyValueEntity entity = repository.findByKey(key)
                .orElseThrow(() -> new NotFoundException("Key not found: " + key));
        return entity.getValue();
    }
}
