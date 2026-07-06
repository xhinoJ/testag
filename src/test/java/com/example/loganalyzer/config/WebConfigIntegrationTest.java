package com.example.loganalyzer.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class WebConfigIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void healthEndpointShouldReturnOk() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build();

        mockMvc.perform(get("/api/logs/health"))
            .andExpect(status().isOk());
    }
}
