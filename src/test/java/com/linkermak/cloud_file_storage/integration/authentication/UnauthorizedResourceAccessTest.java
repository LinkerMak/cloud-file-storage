package com.linkermak.cloud_file_storage.integration.authentication;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UnauthorizedResourceAccessTest extends AbstractIntegrationTest {

    @Test
    void getResourceInfoReturns401WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/resource")
                        .param("path", "/file.txt"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteResourceReturns401WhenUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/resource")
                        .param("path", "/file.txt"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchResourcesReturns401WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/resource/search")
                        .param("query", "file"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void moveResourceReturns401WhenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/resource/move")
                        .param("from", "/from/file.txt")
                        .param("to", "/to/file.txt"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createDirectoryReturns401WhenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/directory")
                        .param("path", "/new-directory"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getDirectoryContentReturns401WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/directory")
                        .param("path", "/"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}