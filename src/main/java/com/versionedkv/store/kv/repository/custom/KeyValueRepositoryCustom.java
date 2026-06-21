package com.versionedkv.store.kv.repository.custom;

import java.util.List;

public interface KeyValueRepositoryCustom {

    void batchUpsert(List<String> keys, List<String> values);
}
