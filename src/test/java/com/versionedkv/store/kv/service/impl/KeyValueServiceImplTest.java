package com.versionedkv.store.kv.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.versionedkv.store.kv.dto.KeyValueRecord;
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
    void create_emptyBody_throwsIllegalArgumentException() throws Exception {
        JsonNode body = objectMapper.readTree("{}");

        assertThatThrownBy(() -> service.create(body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one key-value pair");
    }

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
    void create_jsonObjectValueSerializationFails_throwsIllegalArgumentException() throws Exception {
        JsonNode body = objectMapper.readTree("{\"mykey\": {\"name\": \"test\"}}");
        doThrow(new JsonProcessingException("fail") {}).when(objectMapper).writeValueAsString(any());

        assertThatThrownBy(() -> service.create(body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to serialize value for key: mykey");
    }

    @Test
    void create_jsonObjectValue_savesAsJsonString() throws Exception {
        JsonNode body = objectMapper.readTree("{\"mykey\": {\"name\": \"test\", \"age\": 30}}");
        doNothing().when(repository).batchUpsert(anyList(), anyList());
        KeyValueEntity entity = new KeyValueEntity("mykey", "{\"name\":\"test\",\"age\":30}");
        entity.setVersion(1L);
        when(repository.findByKeyIn(List.of("mykey"))).thenReturn(List.of(entity));

        service.create(body);

        verify(versionRepository).saveAll(versionListCaptor.capture());
        List<KeyValueVersionEntity> versions = versionListCaptor.getValue();
        assertThat(versions).hasSize(1);
        assertThat(versions.get(0).getValue()).isEqualTo("{\"name\":\"test\",\"age\":30}");
    }

    @Test
    void getByKey_found_returnsValue() {
        KeyValueEntity entity = new KeyValueEntity("mykey", "value1");
        when(repository.findByKey("mykey")).thenReturn(Optional.of(entity));

        KeyValueRecord actual = service.getByKey("mykey");

        assertThat(actual.key()).isEqualTo("mykey");
        assertThat(actual.value().asText()).isEqualTo("value1");
    }

    @Test
    void getByKey_foundJsonObject_parsesCorrectly() {
        KeyValueEntity entity = new KeyValueEntity("mykey", "{\"name\":\"test\"}");
        when(repository.findByKey("mykey")).thenReturn(Optional.of(entity));

        KeyValueRecord actual = service.getByKey("mykey");

        assertThat(actual.key()).isEqualTo("mykey");
        assertThat(actual.value().get("name").asText()).isEqualTo("test");
    }

    @Test
    void getByKey_notFound_throwsNotFoundException() {
        when(repository.findByKey("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByKey("missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void getByKeyAtTimestamp_found_returnsVersionValue() {
        KeyValueVersionEntity version = new KeyValueVersionEntity("mykey", "oldvalue", 1L);
        when(versionRepository.findTopByKeyAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq("mykey"),
                org.mockito.ArgumentMatchers.any())).thenReturn(Optional.of(version));

        KeyValueRecord actual = service.getByKeyAtTimestamp("mykey", 1440568980L);

        assertThat(actual.key()).isEqualTo("mykey");
        assertThat(actual.version()).isEqualTo(1L);
        assertThat(actual.value().asText()).isEqualTo("oldvalue");
    }

    @Test
    void getByKeyAtTimestamp_notFound_throwsNotFoundException() {
        when(versionRepository.findTopByKeyAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq("mykey"),
                org.mockito.ArgumentMatchers.any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByKeyAtTimestamp("mykey", 1440568980L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No version found");
    }

    @Test
    void getAllRecords_returnsAllRecords() {
        KeyValueEntity e1 = new KeyValueEntity("key1", "val1");
        e1.setVersion(1L);
        KeyValueEntity e2 = new KeyValueEntity("key2", "{\"nested\":true}");
        e2.setVersion(2L);
        when(repository.findAll()).thenReturn(List.of(e1, e2));

        List<KeyValueRecord> actual = service.getAllRecords();

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).key()).isEqualTo("key1");
        assertThat(actual.get(0).version()).isNull();
        assertThat(actual.get(0).value().asText()).isEqualTo("val1");
        assertThat(actual.get(1).key()).isEqualTo("key2");
        assertThat(actual.get(1).version()).isNull();
        assertThat(actual.get(1).value().get("nested").asBoolean()).isTrue();
    }

    @Test
    void getAllRecords_empty_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<KeyValueRecord> actual = service.getAllRecords();

        assertThat(actual).isEmpty();
    }
}
