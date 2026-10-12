package com.dirinekyan.eft;

import com.dirinekyan.eft.domain.Language;
import com.dirinekyan.eft.service.TaskProcessingScheduler;
import com.dirinekyan.eft.speller.SpellError;
import com.dirinekyan.eft.speller.SpellerClient;
import com.dirinekyan.eft.speller.SpellerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Полный цикл на реальном PostgreSQL (Testcontainers): создание задачи через API,
 * обработка планировщиком и получение результата. Яндекс.Спеллер подменён моком.
 */
@SpringBootTest(properties = "app.scheduler.delay=3600000")
@AutoConfigureMockMvc
@Testcontainers
class TaskFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskProcessingScheduler scheduler;

    @MockBean
    private SpellerClient spellerClient;

    @Test
    void taskIsCreatedProcessedAndReturnedWithCorrectedText() throws Exception {
        when(spellerClient.check(eq("helo wrld"), eq(Language.EN), anyInt())).thenReturn(List.of(
                new SpellError(1, 0, 4, "helo", List.of("hello")),
                new SpellError(1, 5, 4, "wrld", List.of("world"))));

        String id = createTask("helo wrld", "EN");

        mockMvc.perform(get("/api/v1/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.correctedText").doesNotExist());

        scheduler.processNewTasks();

        mockMvc.perform(get("/api/v1/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.correctedText").value("hello world"));
    }

    @Test
    void taskGetsErrorStatusWhenSpellerIsUnavailable() throws Exception {
        when(spellerClient.check(eq("some text here"), eq(Language.EN), anyInt()))
                .thenThrow(new SpellerException("Speller is unavailable"));

        String id = createTask("some text here", "EN");
        scheduler.processNewTasks();

        mockMvc.perform(get("/api/v1/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.errorMessage").value("Speller is unavailable"))
                .andExpect(jsonPath("$.correctedText").doesNotExist());
    }

    private String createTask(String text, String language) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("text", text, "language", language));
        String response = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }
}
