package com.dirinekyan.eft.repository;

import com.dirinekyan.eft.domain.CorrectionTask;
import com.dirinekyan.eft.domain.TaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CorrectionTaskRepository extends JpaRepository<CorrectionTask, UUID> {

    List<CorrectionTask> findByStatusOrderByCreatedAtAsc(TaskStatus status, Pageable pageable);
}
