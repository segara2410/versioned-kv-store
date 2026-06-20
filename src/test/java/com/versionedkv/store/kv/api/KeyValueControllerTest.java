package com.versionedkv.store.kv.api;

import com.versionedkv.store.kv.service.KeyValueService;
import com.versionedkv.store.shared.api.GlobalExceptionHandler;
import com.versionedkv.store.shared.api.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    void post_validRequest_returns200AndValue() throws Exception {
        given(service.create(org.mockito.ArgumentMatchers.any())).willReturn("value1");

        mvc.perform(post("/object")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mykey\": \"value1\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("value1"));
    }

    @Test
    void post_emptyBody_returns400() throws Exception {
        mvc.perform(post("/object")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getByKey_existing_returns200AndValue() throws Exception {
        given(service.getByKey("mykey")).willReturn("value1");

        mvc.perform(get("/object/mykey"))
                .andExpect(status().isOk())
                .andExpect(content().string("value1"));
    }

    @Test
    void getByKey_missing_returns404ProblemDetail() throws Exception {
        given(service.getByKey("missing")).willThrow(new NotFoundException("Key not found: missing"));

        mvc.perform(get("/object/missing"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Key not found: missing"));
    }
}
