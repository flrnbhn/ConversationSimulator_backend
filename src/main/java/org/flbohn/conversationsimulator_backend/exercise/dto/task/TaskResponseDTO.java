package org.flbohn.conversationsimulator_backend.exercise.dto.task;

import org.flbohn.conversationsimulator_backend.exercise.domain.Task;

public record TaskResponseDTO(
        String description
) {
    public static TaskResponseDTO from(Task task) {
        return new TaskResponseDTO(task.getDescription());
    }
}
