package com.versionedkv.store.kv.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

public record KeyValueRecord(String key, @JsonInclude(JsonInclude.Include.NON_NULL) Long version, JsonNode value) {

    public KeyValueRecord(String key, JsonNode value) {
        this(key, null, value);
    }
}
