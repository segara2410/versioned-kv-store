package com.versionedkv.store.kv.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.versionedkv.store.kv.dto.KeyValueRecord;
import com.versionedkv.store.kv.service.KeyValueService;
import com.versionedkv.store.shared.api.GlobalExceptionHandler;
import com.versionedkv.store.shared.api.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KeyValueController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KeyValueControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private KeyValueService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void post_validRequest_returns200() throws Exception {
        mvc.perform(post("/object")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mykey\": \"value1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void post_emptyBody_returns400() throws Exception {
        mvc.perform(post("/object")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Request body must contain at least one key-value pair"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(service, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getByKey_existing_returns200AndValue() throws Exception {
        JsonNode valueNode = objectMapper.valueToTree("value1");
        given(service.getByKey("mykey")).willReturn(valueNode);

        mvc.perform(get("/object/mykey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value("value1"));
    }

    @Test
    void getByKey_missing_returns404() throws Exception {
        given(service.getByKey("missing")).willThrow(new NotFoundException("Key not found: missing"));

        mvc.perform(get("/object/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Key not found: missing"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getByKey_withTimestamp_returns200AndValue() throws Exception {
        JsonNode valueNode = objectMapper.readTree("{\"name\": \"test\"}");
        KeyValueRecord record = new KeyValueRecord("mykey", 3L, valueNode);
        given(service.getByKeyAtTimestamp("mykey", 1440568980L)).willReturn(record);

        mvc.perform(get("/object/mykey?timestamp=1440568980"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.key").value("mykey"))
                .andExpect(jsonPath("$.data.version").value(3))
                .andExpect(jsonPath("$.data.value.name").value("test"));
    }

    @Test
    void getByKey_unexpectedException_returns500() throws Exception {
        given(service.getByKey("mykey")).willThrow(new RuntimeException("boom"));

        mvc.perform(get("/object/mykey"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getAllRecords_returns200AndArray() throws Exception {
        JsonNode value1 = objectMapper.valueToTree("v1");
        JsonNode value2 = objectMapper.valueToTree(42);
        given(service.getAllRecords()).willReturn(List.of(
                new KeyValueRecord("k1", 1L, value1),
                new KeyValueRecord("k2", 2L, value2)
        ));

        mvc.perform(get("/object/get_all_records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].key").value("k1"))
                .andExpect(jsonPath("$.data[0].value").value("v1"))
                .andExpect(jsonPath("$.data[1].key").value("k2"))
                .andExpect(jsonPath("$.data[1].value").value(42));
    }
}
