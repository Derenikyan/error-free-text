package com.dirinekyan.eft.api;

import com.dirinekyan.eft.domain.CorrectionTask;
import com.dirinekyan.eft.domain.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Результат задачи: для COMPLETED — исправленный текст, для ERROR — описание ошибки,
 * для задач в обработке — только статус.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskResultResponse(TaskStatus status, String correctedText, String errorMessage) {

    public static TaskResultResponse from(CorrectionTask task) {
        return switch (task.getStatus()) {
            case COMPLETED -> new TaskResultResponse(task.getStatus(), task.getCorrectedText(), null);
            case ERROR -> new TaskResultResponse(task.getStatus(), null, task.getErrorMessage());
            case NEW, IN_PROGRESS -> new TaskResultResponse(task.getStatus(), null, null);
        };
    }
}
