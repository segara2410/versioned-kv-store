package com.versionedkv.store.kv.repository.custom.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeyValueRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private KeyValueRepositoryImpl repository;

    @Test
    void batchUpsert_singleKey_buildsAndExecutesCorrectSql() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        repository.batchUpsert(List.of("key1"), List.of("value1"));

        verify(entityManager).createNativeQuery(sqlCaptor());
        verify(query).setParameter("key0", "key1");
        verify(query).setParameter("value0", "value1");
        verify(query).executeUpdate();
    }

    @Test
    void batchUpsert_multipleKeys_buildsCommaSeparatedValues() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(2);

        repository.batchUpsert(List.of("key1", "key2"), List.of("value1", "value2"));

        verify(entityManager).createNativeQuery(sqlCaptor());
        verify(query).setParameter("key0", "key1");
        verify(query).setParameter("value0", "value1");
        verify(query).setParameter("key1", "key2");
        verify(query).setParameter("value1", "value2");
        verify(query).executeUpdate();

        String sql = captureSql();
        assertThat(sql).contains("VALUES (:key0, :value0, 1), (:key1, :value1, 1)");
        assertThat(sql).contains("ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, version = key_values.version + 1");
    }

    private String captureSql() {
        org.mockito.ArgumentCaptor<String> captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(captor.capture());
        return captor.getValue();
    }

    private static String sqlCaptor() {
        return org.mockito.ArgumentMatchers.anyString();
    }
}
