package com.dirinekyan.eft.service;

import com.dirinekyan.eft.api.CreateTaskRequest;
import com.dirinekyan.eft.api.TaskResultResponse;
import com.dirinekyan.eft.domain.CorrectionTask;
import com.dirinekyan.eft.domain.Language;
import com.dirinekyan.eft.domain.TaskStatus;
import com.dirinekyan.eft.exception.TaskNotFoundException;
import com.dirinekyan.eft.repository.CorrectionTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final CorrectionTaskRepository repository;

    public TaskService(CorrectionTaskRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UUID create(CreateTaskRequest request) {
        CorrectionTask task = repository.save(new CorrectionTask(request.text(), Language.valueOf(request.language())));
        log.info("Created correction task {} (lang={}, length={})", task.getId(), task.getLanguage(),
                task.getText().length());
        return task.getId();
    }

    @Transactional(readOnly = true)
    public TaskResultResponse getResult(UUID id) {
        return repository.findById(id)
                .map(TaskResultResponse::from)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    /**
     * Забирает пачку новых задач и переводит их в IN_PROGRESS одной транзакцией.
     */
    @Transactional
    public List<CorrectionTask> claimNewTasks(int batchSize) {
        List<CorrectionTask> tasks = repository.findByStatusOrderByCreatedAtAsc(TaskStatus.NEW,
                PageRequest.of(0, batchSize));
        tasks.forEach(CorrectionTask::markInProgress);
        return repository.saveAll(tasks);
    }

    @Transactional
    public void save(CorrectionTask task) {
        repository.save(task);
    }
}
