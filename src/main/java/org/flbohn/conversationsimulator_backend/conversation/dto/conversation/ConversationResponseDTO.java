package org.flbohn.conversationsimulator_backend.conversation.dto.conversation;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;
import org.flbohn.conversationsimulator_backend.conversation.types.Grade;
import org.flbohn.conversationsimulator_backend.exercise.dto.exercise.ExerciseResponseDTO;

import java.util.Date;

public record ConversationResponseDTO(
        Date conversationStartDate,
        Grade gradeOfConversation,
        Integer pointsOfConversation,
        ExerciseResponseDTO exerciseResponseDTO
) {
    public static ConversationResponseDTO from(Conversation conversation) {
        return new ConversationResponseDTO(
                conversation.getConversationStartDate(),
                conversation.getGradeOfConversation(),
                conversation.getPointsOfConversation(),
                ExerciseResponseDTO.from(conversation.getExercise())
        );
    }
}
