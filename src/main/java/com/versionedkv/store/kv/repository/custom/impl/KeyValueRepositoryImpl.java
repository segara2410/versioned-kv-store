package com.versionedkv.store.kv.repository.custom.impl;

import com.versionedkv.store.kv.repository.custom.KeyValueRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KeyValueRepositoryImpl implements KeyValueRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void batchUpsert(List<String> keys, List<String> values) {
        StringBuilder sql = new StringBuilder("INSERT INTO key_values (key, value, version) VALUES ");
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("(:key").append(i).append(", :value").append(i).append(", 1)");
        }
        sql.append(" ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, version = key_values.version + 1");

        Query query = entityManager.createNativeQuery(sql.toString());
        for (int i = 0; i < keys.size(); i++) {
            query.setParameter("key" + i, keys.get(i));
            query.setParameter("value" + i, values.get(i));
        }
        query.executeUpdate();
    }
}
