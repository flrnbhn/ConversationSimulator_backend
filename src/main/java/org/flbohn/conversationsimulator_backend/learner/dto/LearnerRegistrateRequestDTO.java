package org.flbohn.conversationsimulator_backend.learner.dto;

public record LearnerRegistrateRequestDTO(
        String name,
        String learningLanguage
) {
}
