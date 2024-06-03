package org.flbohn.conversationsimulator_backend.learner.dto;

import org.flbohn.conversationsimulator_backend.learner.types.LearningLanguage;

public record LearnerRegistrateRequestDTO(
        String name,
        LearningLanguage learningLanguage
) {
}
