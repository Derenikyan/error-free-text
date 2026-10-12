package com.dirinekyan.eft.service;

import com.dirinekyan.eft.domain.CorrectionTask;
import com.dirinekyan.eft.domain.Language;
import com.dirinekyan.eft.domain.TaskStatus;
import com.dirinekyan.eft.speller.SpellerException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskProcessingSchedulerTest {

    private final TaskService taskService = mock(TaskService.class);
    private final TextCorrectionService correctionService = mock(TextCorrectionService.class);
    private final TaskProcessingScheduler scheduler = new TaskProcessingScheduler(taskService, correctionService, 10);

    @Test
    void completesTaskWithCorrectedText() {
        CorrectionTask task = new CorrectionTask("превет", Language.RU);
        when(taskService.claimNewTasks(10)).thenReturn(List.of(task));
        when(correctionService.correct("превет", Language.RU)).thenReturn("привет");

        scheduler.processNewTasks();

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals("привет", task.getCorrectedText());
        verify(taskService).save(task);
    }

    @Test
    void marksTaskAsErrorWhenSpellerFails() {
        CorrectionTask task = new CorrectionTask("текст", Language.RU);
        when(taskService.claimNewTasks(10)).thenReturn(List.of(task));
        when(correctionService.correct("текст", Language.RU)).thenThrow(new SpellerException("timeout"));

        scheduler.processNewTasks();

        assertEquals(TaskStatus.ERROR, task.getStatus());
        assertEquals("timeout", task.getErrorMessage());
        assertNull(task.getCorrectedText());
        verify(taskService).save(task);
    }

    @Test
    void doesNothingWhenNoNewTasks() {
        when(taskService.claimNewTasks(10)).thenReturn(List.of());

        scheduler.processNewTasks();

        verify(correctionService, never()).correct(any(), any());
        verify(taskService, never()).save(any());
    }
}
