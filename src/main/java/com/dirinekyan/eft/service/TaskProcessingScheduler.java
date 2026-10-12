package com.dirinekyan.eft.service;

import com.dirinekyan.eft.domain.CorrectionTask;
import com.dirinekyan.eft.speller.SpellerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Периодически забирает необработанные задачи и отправляет их тексты в Спеллер.
 * При ошибке обращения к API задача получает статус ERROR, и её обработка прекращается.
 */
@Component
public class TaskProcessingScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskProcessingScheduler.class);

    private final TaskService taskService;
    private final TextCorrectionService correctionService;
    private final int batchSize;

    public TaskProcessingScheduler(TaskService taskService,
                                   TextCorrectionService correctionService,
                                   @Value("${app.scheduler.batch-size:10}") int batchSize) {
        this.taskService = taskService;
        this.correctionService = correctionService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.delay:10000}")
    public void processNewTasks() {
        List<CorrectionTask> tasks = taskService.claimNewTasks(batchSize);
        if (tasks.isEmpty()) {
            return;
        }
        log.info("Processing {} new task(s)", tasks.size());
        tasks.forEach(this::process);
    }

    void process(CorrectionTask task) {
        try {
            task.complete(correctionService.correct(task.getText(), task.getLanguage()));
            log.info("Task {} completed", task.getId());
        } catch (SpellerException e) {
            task.fail(e.getMessage());
            log.warn("Task {} failed: {}", task.getId(), e.getMessage());
        } catch (RuntimeException e) {
            task.fail("Внутренняя ошибка при обработке задачи");
            log.error("Task {} failed with unexpected error", task.getId(), e);
        }
        taskService.save(task);
    }
}
