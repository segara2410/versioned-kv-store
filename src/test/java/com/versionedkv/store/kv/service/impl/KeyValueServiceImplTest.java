package com.versionedkv.store.kv.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.versionedkv.store.kv.repository.entity.KeyValueEntity;
import com.versionedkv.store.kv.repository.entity.KeyValueVersionEntity;
import com.versionedkv.store.kv.repository.KeyValueRepository;
import com.versionedkv.store.kv.repository.KeyValueVersionRepository;
import com.versionedkv.store.shared.api.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeyValueServiceImplTest {

    @Mock
    private KeyValueRepository repository;

    @Mock
    private KeyValueVersionRepository versionRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private KeyValueServiceImpl service;

    @Captor
    private ArgumentCaptor<List<KeyValueVersionEntity>> versionListCaptor;

    @Test
    void create_newKey_savesWithVersion1() throws Exception {
        JsonNode body = objectMapper.readTree("{\"mykey\": \"value1\"}");
        doNothing().when(repository).batchUpsert(anyList(), anyList());
        KeyValueEntity entity = new KeyValueEntity("mykey", "value1");
        entity.setVersion(1L);
        when(repository.findByKeyIn(List.of("mykey"))).thenReturn(List.of(entity));

        service.create(body);

        verify(versionRepository).saveAll(versionListCaptor.capture());
        List<KeyValueVersionEntity> versions = versionListCaptor.getValue();
        assertThat(versions).hasSize(1);
        assertThat(versions.get(0).getVersion()).isEqualTo(1L);
        assertThat(versions.get(0).getValue()).isEqualTo("value1");
        assertThat(versions.get(0).getKey()).isEqualTo("mykey");
    }

    @Test
    void create_multipleKeys_savesAll() throws Exception {
        JsonNode body = objectMapper.readTree("{\"key1\": \"val1\", \"key2\": \"val2\"}");
        doNothing().when(repository).batchUpsert(anyList(), anyList());
        KeyValueEntity e1 = new KeyValueEntity("key1", "val1");
        e1.setVersion(1L);
        KeyValueEntity e2 = new KeyValueEntity("key2", "val2");
        e2.setVersion(2L);
        when(repository.findByKeyIn(List.of("key1", "key2"))).thenReturn(List.of(e1, e2));

        service.create(body);

        verify(versionRepository).saveAll(versionListCaptor.capture());
        List<KeyValueVersionEntity> versions = versionListCaptor.getValue();
        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).getKey()).isEqualTo("key1");
        assertThat(versions.get(0).getVersion()).isEqualTo(1L);
        assertThat(versions.get(1).getKey()).isEqualTo("key2");
        assertThat(versions.get(1).getVersion()).isEqualTo(2L);
    }

    @Test
    void create_existingKey_incrementsVersionAndSavesSnapshot() throws Exception {
        JsonNode body = objectMapper.readTree("{\"mykey\": \"newvalue\"}");
        doNothing().when(repository).batchUpsert(anyList(), anyList());
        KeyValueEntity entity = new KeyValueEntity("mykey", "newvalue");
        entity.setVersion(2L);
        when(repository.findByKeyIn(List.of("mykey"))).thenReturn(List.of(entity));

        service.create(body);

        verify(versionRepository).saveAll(versionListCaptor.capture());
        List<KeyValueVersionEntity> versions = versionListCaptor.getValue();
        assertThat(versions).hasSize(1);
        assertThat(versions.get(0).getVersion()).isEqualTo(2L);
        assertThat(versions.get(0).getValue()).isEqualTo("newvalue");
    }

    @Test
    void getByKey_found_returnsValue() {
        KeyValueEntity entity = new KeyValueEntity("mykey", "value1");
        when(repository.findByKey("mykey")).thenReturn(Optional.of(entity));

        String actual = service.getByKey("mykey");

        assertThat(actual).isEqualTo("value1");
    }

    @Test
    void getByKey_notFound_throwsNotFoundException() {
        when(repository.findByKey("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByKey("missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("missing");
    }
}
