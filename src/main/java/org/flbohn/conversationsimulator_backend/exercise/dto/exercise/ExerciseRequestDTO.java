package org.flbohn.conversationsimulator_backend.exercise.dto.exercise;

import org.flbohn.conversationsimulator_backend.exercise.dto.task.TaskRequestDTO;

import java.util.List;

public record ExerciseRequestDTO(
        String title,
        String szenario,
        String furtherInformation,
        String roleUser,
        String roleSystem,
        Integer numberOfMessagesTillFailure,
        List<TaskRequestDTO> taskRequestDTO
) {

}
