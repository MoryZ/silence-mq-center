package com.old.silence.mq.center.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class DashboardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testBroker() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/dashboard/broker")
                        .param("date", "2024-01-01"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testTopic() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/dashboard/topic")
                        .param("date", "2024-01-01"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testTopicWithTopicName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/dashboard/topic")
                        .param("date", "2024-01-01")
                        .param("topicName", "test-topic"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testTopicCurrent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/dashboard/topicCurrent"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
