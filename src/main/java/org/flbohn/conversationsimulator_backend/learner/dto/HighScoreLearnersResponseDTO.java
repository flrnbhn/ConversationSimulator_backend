package org.flbohn.conversationsimulator_backend.learner.dto;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;

public record HighScoreLearnersResponseDTO(
        String name,
        Long anz
) {
    public static HighScoreLearnersResponseDTO from(Conversation conversation) {
        return new HighScoreLearnersResponseDTO(
                conversation.getLearner().getName(),
                (long) conversation.getMessagesOfConversation().size() - 1
        );
    }
}
