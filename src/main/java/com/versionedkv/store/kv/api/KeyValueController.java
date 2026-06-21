package com.versionedkv.store.kv.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionedkv.store.kv.service.KeyValueService;
import com.versionedkv.store.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/object")
@RequiredArgsConstructor
public class KeyValueController {

    private final KeyValueService service;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@RequestBody JsonNode body) {
        if (body == null || !body.fields().hasNext()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Request body must contain at least one key-value pair"));
        }
        service.create(body);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<String>> getByKey(@PathVariable String key) {
        String value = service.getByKey(key);
        return ResponseEntity.ok(ApiResponse.success(value));
    }
}
