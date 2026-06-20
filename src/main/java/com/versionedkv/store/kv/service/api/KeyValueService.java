package com.versionedkv.store.kv.service.api;

import com.fasterxml.jackson.databind.JsonNode;

public interface KeyValueService {

    String create(JsonNode body);

    String getByKey(String key);
}
