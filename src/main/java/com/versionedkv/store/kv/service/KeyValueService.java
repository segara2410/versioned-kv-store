package com.versionedkv.store.kv.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface KeyValueService {

    void create(JsonNode body);

    String getByKey(String key);
}
