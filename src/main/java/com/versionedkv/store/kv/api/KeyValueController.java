package com.versionedkv.store.kv.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.versionedkv.store.kv.dto.KeyValueRecord;
import com.versionedkv.store.kv.service.KeyValueService;
import com.versionedkv.store.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse<?>> getByKey(
            @PathVariable String key,
            @RequestParam(name = "timestamp", required = false) Long timestamp) {
        if (timestamp != null) {
            KeyValueRecord record = service.getByKeyAtTimestamp(key, timestamp);
            return ResponseEntity.ok(ApiResponse.success(record));
        }
        JsonNode value = service.getByKey(key);
        return ResponseEntity.ok(ApiResponse.success(value));
    }

    @GetMapping("/get_all_records")
    public ResponseEntity<ApiResponse<List<KeyValueRecord>>> getAllRecords() {
        List<KeyValueRecord> records = service.getAllRecords();
        return ResponseEntity.ok(ApiResponse.success(records));
    }
}
