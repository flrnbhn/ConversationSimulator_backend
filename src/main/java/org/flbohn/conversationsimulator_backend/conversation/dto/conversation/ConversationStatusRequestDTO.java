package org.flbohn.conversationsimulator_backend.conversation.dto.conversation;

import org.flbohn.conversationsimulator_backend.conversation.types.ConversationStatus;

public record ConversationStatusRequestDTO(
        ConversationStatus conversationStatus
) {
}
