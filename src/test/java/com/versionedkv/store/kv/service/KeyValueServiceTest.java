package com.versionedkv.store.kv.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.versionedkv.store.kv.repository.KeyValueEntity;
import com.versionedkv.store.kv.repository.KeyValueRepository;
import com.versionedkv.store.shared.api.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeyValueServiceTest {

    @Mock
    private KeyValueRepository repository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private KeyValueService service;

    @Test
    void create_newKey_savesAndReturnsValue() throws Exception {
        JsonNode body = objectMapper.readTree("{\"mykey\": \"value1\"}");
        when(repository.findByKey("mykey")).thenReturn(Optional.empty());
        when(repository.save(any(KeyValueEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        String actual = service.create(body);

        assertThat(actual).isEqualTo("value1");
        verify(repository).save(any(KeyValueEntity.class));
    }

    @Test
    void create_existingKey_updatesAndReturnsValue() throws Exception {
        JsonNode body = objectMapper.readTree("{\"mykey\": \"newvalue\"}");
        KeyValueEntity existing = new KeyValueEntity("mykey", "oldvalue");
        when(repository.findByKey("mykey")).thenReturn(Optional.of(existing));
        when(repository.save(any(KeyValueEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        String actual = service.create(body);

        assertThat(actual).isEqualTo("newvalue");
        assertThat(existing.getValue()).isEqualTo("newvalue");
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
