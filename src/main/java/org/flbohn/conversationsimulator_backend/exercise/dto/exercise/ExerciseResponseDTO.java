package org.flbohn.conversationsimulator_backend.exercise.dto.exercise;

import org.flbohn.conversationsimulator_backend.exercise.domain.Exercise;
import org.flbohn.conversationsimulator_backend.exercise.dto.task.TaskResponseDTO;

import java.util.ArrayList;
import java.util.List;

public record ExerciseResponseDTO(
        Long exerciseId,
        String title,
        String szenario,
        String furtherInformation,
        List<TaskResponseDTO> taskResponseDTO
) {
    public static ExerciseResponseDTO from(Exercise exercise) {
        return new ExerciseResponseDTO(
                exercise.getId(),
                exercise.getTitle(),
                exercise.getSzenario(),
                exercise.getFurtherInformation(),
                new ArrayList<>(exercise.getTasks().stream()
                        .map(TaskResponseDTO::from)
                        .toList())
        );
    }
}
