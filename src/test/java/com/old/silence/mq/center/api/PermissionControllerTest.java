package com.old.silence.mq.center.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class PermissionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetMyPermissions() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/permissions/my-permissions"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testCheckPermission() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/permissions/check")
                .param("userId", "1")
                .param("permissionCode", "READ"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // 更多接口测试可按需补充
}
