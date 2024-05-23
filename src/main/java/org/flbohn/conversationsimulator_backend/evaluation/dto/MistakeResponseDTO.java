package org.flbohn.conversationsimulator_backend.evaluation.dto;

import java.util.List;

public record MistakeResponseDTO(
        String message,
        String shortMessage,
        List<String> replacements,
        int offset,
        int length,
        String sentence
) {
}
