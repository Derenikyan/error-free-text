package com.dirinekyan.eft.api;

import com.dirinekyan.eft.domain.TaskStatus;
import com.dirinekyan.eft.exception.TaskNotFoundException;
import com.dirinekyan.eft.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void createsTask() throws Exception {
        UUID id = UUID.randomUUID();
        when(taskService.create(any())).thenReturn(id);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"превет мир\",\"language\":\"RU\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void rejectsUnsupportedLanguage() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\",\"language\":\"DE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/v1/tasks"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    void rejectsTooShortText() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"ab\",\"language\":\"EN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsTextOfDigitsAndSymbolsOnly() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"123 !!! ???\",\"language\":\"RU\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void returnsOnlyStatusForTaskInProgress() throws Exception {
        UUID id = UUID.randomUUID();
        when(taskService.getResult(id)).thenReturn(new TaskResultResponse(TaskStatus.IN_PROGRESS, null, null));

        mockMvc.perform(get("/api/v1/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.correctedText").doesNotExist())
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void returnsCorrectedTextForCompletedTask() throws Exception {
        UUID id = UUID.randomUUID();
        when(taskService.getResult(id)).thenReturn(new TaskResultResponse(TaskStatus.COMPLETED, "hello world", null));

        mockMvc.perform(get("/api/v1/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.correctedText").value("hello world"));
    }

    @Test
    void returnsNotFoundForUnknownTask() throws Exception {
        UUID id = UUID.randomUUID();
        when(taskService.getResult(id)).thenThrow(new TaskNotFoundException(id));

        mockMvc.perform(get("/api/v1/tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TASK_NOT_FOUND"));
    }

    @Test
    void returnsBadRequestForInvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PARAMETER"));
    }
}
