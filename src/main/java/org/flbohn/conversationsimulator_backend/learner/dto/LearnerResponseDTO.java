package org.flbohn.conversationsimulator_backend.learner.dto;

import org.flbohn.conversationsimulator_backend.conversation.types.Grade;
import org.flbohn.conversationsimulator_backend.learner.domain.Learner;

import java.util.List;

public record LearnerResponseDTO(
        String name,
        String learningLanguage,
        Integer totalPoints,
        List<Grade> allGrades,
        Float gradeAverage
) {
    public static LearnerResponseDTO from(Learner learner) {
        return new LearnerResponseDTO(
                learner.getName(),
                learner.getLearningLanguage(),
                learner.getTotalPoints(),
                learner.getAllGrades(),
                learner.getGradeAverage()
        );
    }
}
