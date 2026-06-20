package com.versionedkv.store;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VersionedKvStoreApplicationIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void contextLoads() {
        // Verifies the Spring context starts successfully
    }

    @Test
    void fullLifecycle_postThenGet_returnsPersistedData() throws Exception {
        // Create a key-value pair
        mvc.perform(post("/object")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hello\": \"world\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("world"));

        // Retrieve the created key-value pair
        mvc.perform(get("/object/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("world"));
    }
}
