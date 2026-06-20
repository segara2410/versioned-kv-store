package com.versionedkv.store.kv.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionedkv.store.kv.service.api.KeyValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/object")
@RequiredArgsConstructor
public class KeyValueController {

    private final KeyValueService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody JsonNode body) {
        if (body == null || !body.fields().hasNext()) {
            return ResponseEntity.badRequest().body("Request body must contain at least one key-value pair");
        }
        String value = service.create(body);
        return ResponseEntity.ok(value);
    }

    @GetMapping("/{key}")
    public ResponseEntity<String> getByKey(@PathVariable String key) {
        String value = service.getByKey(key);
        return ResponseEntity.ok(value);
    }
}
