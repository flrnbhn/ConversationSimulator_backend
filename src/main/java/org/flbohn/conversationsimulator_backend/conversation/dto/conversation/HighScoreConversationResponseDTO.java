package org.flbohn.conversationsimulator_backend.conversation.dto.conversation;

import org.flbohn.conversationsimulator_backend.conversation.domain.Conversation;


public record HighScoreConversationResponseDTO(
        Long conversationId,
        String szenario,
        String roleUser,
        String roleSystem
) {
    public static HighScoreConversationResponseDTO from(Conversation conversation) {
        return new HighScoreConversationResponseDTO(
                conversation.getId(),
                conversation.getSzenario(),
                conversation.getRoleUser(),
                conversation.getRoleSystem()
        );
    }
}
