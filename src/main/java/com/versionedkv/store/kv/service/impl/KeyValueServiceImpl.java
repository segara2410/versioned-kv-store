package com.versionedkv.store.kv.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.versionedkv.store.kv.dto.KeyValueRecord;
import com.versionedkv.store.kv.repository.KeyValueRepository;
import com.versionedkv.store.kv.repository.KeyValueVersionRepository;
import com.versionedkv.store.kv.repository.entity.KeyValueEntity;
import com.versionedkv.store.kv.repository.entity.KeyValueVersionEntity;
import com.versionedkv.store.kv.service.KeyValueService;
import com.versionedkv.store.shared.api.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeyValueServiceImpl implements KeyValueService {

    private final KeyValueRepository repository;
    private final KeyValueVersionRepository versionRepository;
    private final ObjectMapper objectMapper;

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
            if (entry.getValue().isTextual()) {
                values.add(entry.getValue().asText());
            } else {
                try {
                    values.add(objectMapper.writeValueAsString(entry.getValue()));
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("Failed to serialize value for key: " + entry.getKey());
                }
            }
        }

        repository.batchUpsert(keys, values);

        List<KeyValueVersionEntity> versions = repository.findByKeyIn(keys).stream()
                .map(e -> new KeyValueVersionEntity(e.getKey(), values.get(keys.indexOf(e.getKey())), e.getVersion()))
                .toList();
        versionRepository.saveAll(versions);
    }

    @Override
    public KeyValueRecord getByKey(String key) {
        KeyValueEntity entity = repository.findByKey(key)
                .orElseThrow(() -> new NotFoundException("Key not found: " + key));
        return new KeyValueRecord(entity.getKey(), parseValue(entity.getValue()));
    }

    @Override
    public KeyValueRecord getByKeyAtTimestamp(String key, long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        KeyValueVersionEntity version = versionRepository
                .findTopByKeyAndCreatedAtLessThanEqualOrderByCreatedAtDesc(key, instant)
                .orElseThrow(() -> new NotFoundException("No version found for key: " + key + " at timestamp: " + timestamp));
        return new KeyValueRecord(version.getKey(), version.getVersion(), parseValue(version.getValue()));
    }

    @Override
    public List<KeyValueRecord> getAllRecords() {
        return repository.findAll().stream()
                .map(e -> new KeyValueRecord(e.getKey(), parseValue(e.getValue())))
                .toList();
    }

    private JsonNode parseValue(String value) {
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException e) {
            return objectMapper.valueToTree(value);
        }
    }
}
