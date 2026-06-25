package com.versionedkv.store.kv.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionedkv.store.kv.dto.KeyValueRecord;

import java.util.List;

public interface KeyValueService {

    void create(JsonNode body);

    KeyValueRecord getByKey(String key);

    KeyValueRecord getByKeyAtTimestamp(String key, long timestamp);

    List<KeyValueRecord> getAllRecords();
}
