package org.flbohn.conversationsimulator_backend.evaluation.dto;

import org.flbohn.conversationsimulator_backend.conversation.types.Grade;

import java.util.List;

public record EvaluationResponseDTO(
        List<MistakeResponseDTO> mistakeResponseDTOS,
        Grade grade,
        Integer points
) {
}
